package com.ofss.KycService.service;

import com.ofss.KycService.dto.*;
import com.ofss.KycService.exception.APIException;
import com.ofss.KycService.exception.ResourceNotFoundException;
import com.ofss.KycService.model.KycDoc;
import com.ofss.KycService.model.Status;
import com.ofss.KycService.repository.KycRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class KycServiceImpl implements KycService {

    private final KycRepository kycRepository;
    private final WebClient webClient;
    private final KafkaTemplate<String,KafkaCustomerDTO> accountCreationKafkaTemplate;
    private final KafkaTemplate<String, EmailEventDTO> emailKafkaTemplate;
    private static final String EmailNotificationTopic = "EmailNotificationTopic";
    private static final String CustomerKYCTopic = "CustomerKYCTopic";

    @Override
    public ResponseEntity<ResponseDTO> uploadKyc(KycRequestDTO dto, Long id) {
        KycDoc existingKyc = kycRepository.findByCustomerId(dto.getCustomerId()).orElse(null);
        if(existingKyc!=null && existingKyc.getStatus()== Status.VERIFIED){
            throw new APIException("KYC already VERIFIED for Customer ID: " + dto.getCustomerId(), HttpStatus.CONFLICT);
        }

        if(existingKyc!=null && existingKyc.getStatus()== Status.PENDING){
            throw new APIException("KYC already exists for Customer ID: " + dto.getCustomerId(), HttpStatus.CONFLICT);
        }
        if(existingKyc!=null && existingKyc.getStatus()== Status.REJECTED){
            kycRepository.delete(existingKyc);
        }

        if (dto.getCustomerId() == null) {
            throw new APIException("Customer ID is required for KYC upload", HttpStatus.BAD_REQUEST);
        }
        KycDoc kyc = new KycDoc();
        kyc.setCustomerId(dto.getCustomerId());
        if (dto.getPanFile() != null) {
            kyc.setPanFileBase64(fileToBase64(dto.getPanFile()));
            kyc.setPanFileName(dto.getPanFile().getOriginalFilename());
            kyc.setPanFileType(dto.getPanFile().getContentType());
            kyc.setPanFileSize(dto.getPanFile().getSize());
        }

        if (dto.getAadhaarFile() != null) {
            kyc.setAadhaarFileBase64(fileToBase64(dto.getAadhaarFile()));
            kyc.setAadhaarFileName(dto.getAadhaarFile().getOriginalFilename());
            kyc.setAadhaarFileType(dto.getAadhaarFile().getContentType());
            kyc.setAadhaarFileSize(dto.getAadhaarFile().getSize());
        }

        if (dto.getPhotoFile() != null) {
            kyc.setPhotoFileBase64(fileToBase64(dto.getPhotoFile()));
            kyc.setPhotoFileName(dto.getPhotoFile().getOriginalFilename());
            kyc.setPhotoFileType(dto.getPhotoFile().getContentType());
            kyc.setPhotoFileSize(dto.getPhotoFile().getSize());
        }

        kyc.setStatus(Status.PENDING);
        String remarks="KYC uploaded and pending verification.";
        kyc.setRemarks(remarks);
        CustomerData customer=getCustomer(id); // fetch customer details
        EmailEventDTO pendingMail = EmailEventDTO.builder()
                .to(customer.getEmail())
                .subject("⏳ Your Account Verification is Pending")
                .body("Dear " + customer.getFirstName() + ",\n\n" +
                        "Your KYC verification is currently under review.\n" +
                        "We’ll notify you once the verification is complete.\n\n" +
                        "Expected review time: 24hrs"  + "\n" +
                        "Thank you for your patience.")
                .templateType("ACCOUNT_PENDING")
                .customerName(customer.getFirstName())
                .build();
        emailKafkaTemplate.send(EmailNotificationTopic, pendingMail);
        kyc.setKycDate(LocalDateTime.now());

        KycDoc savedKyc = kycRepository.save(kyc);

        return ResponseEntity.ok(ResponseDTO.builder()
                .success(true)
                .statusCode(HttpStatus.OK.value())
                .message("KYC uploaded successfully")
                .data(savedKyc)
                .build());
    }
    // Helper method to wrap IOException into unchecked APIException
    private String fileToBase64(MultipartFile file) {
        try {
            return Base64.getEncoder().encodeToString(file.getBytes());
        } catch (IOException e) {
            throw new APIException("Error processing file: " + file.getOriginalFilename(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @Override
    public String viewAllImagesAndUpdateStatus(Long customerId, String status, Long verifierId) {

        CustomerData verifier = getCustomer(verifierId);
        CustomerData customer=getCustomer(customerId);
        customer.setCustomerId(String.valueOf(customerId));
//        System.out.println("Fetched Verifier: " + verifier);
        if (verifier == null) {
            throw new APIException("Verifier not found with ID: " + verifierId, HttpStatus.NOT_FOUND);
        }
        System.out.println("Verifier Role: " + verifier);
        if (verifier.getRole() != null && Role.valueOf(verifier.getRole()) == Role.USER) {
            throw new APIException("Access Denied: Only ADMIN can verify KYC documents.", HttpStatus.FORBIDDEN);
        }

        KycDoc doc = kycRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("KYC Document", "Customer ID", customerId.toString()));
        if(doc.getStatus()==Status.VERIFIED){
            throw new APIException("KYC already VERIFIED for Customer ID: " + customerId, HttpStatus.BAD_REQUEST);
        }
        if(doc.getStatus()==Status.REJECTED){
            throw new APIException("KYC already REJECTED for Customer ID: " + customerId, HttpStatus.BAD_REQUEST);
        }
        if(doc.getStatus()==Status.PENDING){
            System.out.println("KYC is in PENDING status for Customer ID: " + customerId);
        }
        // Build HTML dynamically
        StringBuilder html = new StringBuilder();
        html.append("<html><body style='font-family:Arial,sans-serif; text-align:center;'>");
        html.append("<h2>KYC Documents for Customer ID: ").append(customerId).append("</h2><hr/>");

        if (doc.getPanFileBase64() != null) {
            html.append("<h3>PAN Card</h3>")
                    .append("<img src='data:").append(doc.getPanFileType())
                    .append(";base64,").append(doc.getPanFileBase64())
                    .append("' style='max-width:400px; border:1px solid #ccc; margin:10px;'/><br/>");
        }

        if (doc.getAadhaarFileBase64() != null) {
            html.append("<h3>Aadhaar Card</h3>")
                    .append("<img src='data:").append(doc.getAadhaarFileType())
                    .append(";base64,").append(doc.getAadhaarFileBase64())
                    .append("' style='max-width:400px; border:1px solid #ccc; margin:10px;'/><br/>");
        }

        if (doc.getPhotoFileBase64() != null) {
            html.append("<h3>Photo</h3>")
                    .append("<img src='data:").append(doc.getPhotoFileType())
                    .append(";base64,").append(doc.getPhotoFileBase64())
                    .append("' style='max-width:400px; border:1px solid #ccc; margin:10px;'/><br/>");
        }
        html.append("<hr/>");

        if (status != null && !status.isBlank()) {
            try {
                Status newStatus = Status.valueOf(status.toUpperCase()); // validate enum
                doc.setStatus(newStatus);
                if (newStatus == Status.VERIFIED) {
                    doc.setRemarks("KYC Approved by Verifier ID: " + verifierId);
                    KafkaCustomerDTO kafkaCustomerDTO=KafkaCustomerDTO.builder()
                            .VerificationStatus("VERIFIED")
                            .customerData(customer)
                            .build();
                    accountCreationKafkaTemplate.send(CustomerKYCTopic,kafkaCustomerDTO);
                } else if (newStatus == Status.REJECTED) {
                    KafkaCustomerDTO kafkaCustomerDTO=KafkaCustomerDTO.builder()
                            .VerificationStatus("REJECTED")
                            .customerData(customer)
                            .build();
                    EmailEventDTO emailEvent = EmailEventDTO.builder()
                            .to(kafkaCustomerDTO.getCustomerData().getEmail())
                            .subject("⚠️ Your Account Request Was Rejected")
                            .body("Dear " + kafkaCustomerDTO.getCustomerData().getFirstName() +
                                    ", your account creation request was rejected. Please contact support.")
                            .templateType("ACCOUNT_REJECTED")
                            .customerName(kafkaCustomerDTO.getCustomerData().getFirstName())
                            .rejectionReason("KYC verification failed.")
                            .build();
                    emailKafkaTemplate.send(EmailNotificationTopic, emailEvent);
                    doc.setRemarks("KYC Rejected by Verifier ID: " + verifierId);
                }
                doc.setLastModifiedAt(LocalDateTime.now());
                kycRepository.save(doc);

            } catch (IllegalArgumentException e) {
                throw new APIException("Invalid status value: " + status + ". Allowed: VERIFIED, REJECTED, PENDING", HttpStatus.BAD_REQUEST);
            }
        }
        if(doc.getRemarks()!=null){
            html.append("<p><b>Remarks:</b> ").append(doc.getRemarks()).append("</p>");
        }
        html.append("<p style='color:green;'>Status Updated To: <b>")
                .append(status == null ? doc.getStatus() : status.toUpperCase())
                .append("</b></p>");
        html.append("</body></html>");
        return html.toString();
    }
    @Override
    public ResponseEntity<ResponseDTO> updateKYC(KycRequestDTO requestDTO, Long id) {
        KycDoc existingKyc = kycRepository.findByCustomerId(requestDTO.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("KYC Document", "Customer ID", requestDTO.getCustomerId().toString()));
        if(existingKyc.getStatus()== Status.VERIFIED){
            throw new APIException("KYC already VERIFIED for Customer ID: " + requestDTO.getCustomerId(), HttpStatus.BAD_REQUEST);
        }
        if(existingKyc.getStatus()== Status.REJECTED){
            throw new APIException("KYC already REJECTED for Customer ID: " + requestDTO.getCustomerId(), HttpStatus.BAD_REQUEST);
        }
        if(existingKyc.getStatus()== Status.PENDING){
            if (requestDTO.getPanFile() != null) {
                existingKyc.setPanFileBase64(fileToBase64(requestDTO.getPanFile()));
                existingKyc.setPanFileName(requestDTO.getPanFile().getOriginalFilename());
                existingKyc.setPanFileType(requestDTO.getPanFile().getContentType());
                existingKyc.setPanFileSize(requestDTO.getPanFile().getSize());
            }

            if (requestDTO.getAadhaarFile() != null) {
                existingKyc.setAadhaarFileBase64(fileToBase64(requestDTO.getAadhaarFile()));
                existingKyc.setAadhaarFileName(requestDTO.getAadhaarFile().getOriginalFilename());
                existingKyc.setAadhaarFileType(requestDTO.getAadhaarFile().getContentType());
                existingKyc.setAadhaarFileSize(requestDTO.getAadhaarFile().getSize());
            }

            if (requestDTO.getPhotoFile() != null) {
                existingKyc.setPhotoFileBase64(fileToBase64(requestDTO.getPhotoFile()));
                existingKyc.setPhotoFileName(requestDTO.getPhotoFile().getOriginalFilename());
                existingKyc.setPhotoFileType(requestDTO.getPhotoFile().getContentType());
                existingKyc.setPhotoFileSize(requestDTO.getPhotoFile().getSize());
            }
        }
        existingKyc.setLastModifiedAt(LocalDateTime.now());

        KycDoc updatedKyc = kycRepository.save(existingKyc);

        return ResponseEntity.ok(ResponseDTO.builder()
                .success(true)
                .statusCode(HttpStatus.OK.value())
                .message("KYC updated successfully")
                .data(updatedKyc)
                .build());
    }
    @Override
    public String deleteKyc(Long id, Long verifierId) {
        CustomerData verifier = getCustomer(verifierId);
        if (verifier == null) {
            throw new APIException("Verifier not found with ID: " + verifierId, HttpStatus.NOT_FOUND);
        }
        if (verifier.getRole() != null && Role.valueOf(verifier.getRole()) == Role.USER) {
            throw new APIException("Access Denied: Only ADMIN can delete KYC documents.", HttpStatus.FORBIDDEN);
        }

        KycDoc doc = kycRepository.findByCustomerId(id)
                .orElseThrow(() -> new ResourceNotFoundException("KYC Document", "Customer ID", id.toString()));

        kycRepository.delete(doc);

        return "KYC Document for Customer ID: " + id + " has been deleted successfully.";
    }

    private CustomerData getCustomer(Long id) {
        try {
            CustomerResponseDTO response = webClient.get()
                    .uri("/customers/id/{id}", id)
                    .retrieve()
                    .bodyToMono(CustomerResponseDTO.class)
                    .block();

            if (response == null || response.getData() == null) {
                throw new APIException("Customer not found with ID: " + id, HttpStatus.NOT_FOUND);
            }
            return response.getData();

        } catch (Exception e) {
            e.printStackTrace();
            throw new APIException("Error fetching customer with ID: " + id + ". Error: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}