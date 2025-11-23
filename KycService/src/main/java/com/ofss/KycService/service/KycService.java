package com.ofss.KycService.service;

import com.ofss.KycService.dto.KycRequestDTO;
import com.ofss.KycService.dto.ResponseDTO;
import org.springframework.http.ResponseEntity;

public interface KycService {
    ResponseEntity<ResponseDTO> uploadKyc(KycRequestDTO kycRequestDTO, Long id);
    String viewAllImagesAndUpdateStatus(Long CustomerId, String status,Long verifierId);
    ResponseEntity<ResponseDTO> updateKYC(KycRequestDTO requestDTO, Long id);
    String deleteKyc(Long id, Long verifierId);
    ResponseEntity<ResponseDTO> userViewKycStatus(Long id);

    ResponseEntity<ResponseDTO> getAllKycRecords(Long adminId);
}
