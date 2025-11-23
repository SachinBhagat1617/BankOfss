package com.ofss.Customer.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String KeycloakId;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name")
    private String lastName;


    @Column(name = "username", nullable = false, unique = true)
    private String username;


    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Pattern(regexp="^\\+[1-9][0-9]{9,14}$", message="Phone number should contain 10-15 digits")
    @Column(name = "phone")
    private String phone;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role=Role.USER;

    @Column(name = "date_of_birth")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "address_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_user_address"))
    @NotNull(message="Address must be provided")
    private Address address;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt=LocalDateTime.now();

    @Pattern(regexp = "^[0-9]{12}$", message = "Aadhaar number must be exactly 12 digits")
    @Column(name = "aadhaar_number", length = 12, unique = true)
    private String aadhaarNumber;

    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$", message = "Invalid PAN format (e.g., ABCDE1234F)")
    @Column(name = "pan_number", length = 10, unique = true)
    private String panNumber;

    @Column(name = "is_deleted")
    private boolean inActive=false;

    @Column(name = "updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;


}