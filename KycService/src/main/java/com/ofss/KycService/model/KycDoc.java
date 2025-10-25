package com.ofss.KycService.model;



import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "KYCDOC")
@AllArgsConstructor
@NoArgsConstructor
public class KycDoc {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID",nullable = false)
    private Long id;

    @Column(name = "CUSTOMERID", nullable = false)
    private Long customerId;

    @Column(name = "KYC_DATE")
    private LocalDateTime kycDate;

    @Lob
    @Column(name = "PAN_FILE", columnDefinition = "CLOB")
    private String panFileBase64;

    @Lob
    @Column(name = "AADHAAR_FILE", columnDefinition = "CLOB")
    private String aadhaarFileBase64;

    @Lob
    @Column(name = "PHOTO_FILE", columnDefinition = "CLOB")
    private String photoFileBase64;

    @Column(name = "PAN_FILE_NAME")
    private String panFileName;

    @Column(name = "PAN_FILE_TYPE")
    private String panFileType;

    @Column(name = "PAN_FILE_SIZE")
    private Long panFileSize;

    @Column(name = "AADHAAR_FILE_NAME")
    private String aadhaarFileName;

    @Column(name = "AADHAAR_FILE_TYPE")
    private String aadhaarFileType;

    @Column(name = "AADHAAR_FILE_SIZE")
    private Long aadhaarFileSize;

    @Column(name = "PHOTO_FILE_NAME")
    private String photoFileName;

    @Column(name = "PHOTO_FILE_TYPE")
    private String photoFileType;

    @Column(name = "PHOTO_FILE_SIZE")
    private Long photoFileSize;

    @Column(name = "STATUS")
    @Enumerated(EnumType.STRING)
    private Status status=Status.PENDING;

    @Column(name = "REMARKS")
    private String remarks;

    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "LAST_MODIFIED_AT")
    private LocalDateTime lastModifiedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        lastModifiedAt = LocalDateTime.now();
        if (kycDate == null) kycDate = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        lastModifiedAt = LocalDateTime.now();
    }
}
