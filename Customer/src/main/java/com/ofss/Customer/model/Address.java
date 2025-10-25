package com.ofss.Customer.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "CustomerAddress")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "city")
    private String city;

    @Column(name = "street")
    private String street;


    @Column(name = "country")
    private String country;


    @Column(name = "state")
    private String state;

    @Column(name = "pincode")
    private String pincode;
}