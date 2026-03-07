package com.dhar.propertymortgageapi.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="property_transactions")
@Data
public class PropertyTransaction {
    @Id
    @Column(name = "transaction_id", nullable = false, unique = true)
    private String transactionId;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "transfer_date")
    private LocalDateTime transferDate;

    @Column(name = "postcode")
    private String postcode;

    @Column(name = "property_type")
    private String propertyType;

    @Column(name = "town_city")
    private String townCity;

    @Column(name = "county")
    private String county;

    @Column(name = "street")
    private String street;

    @Column(name = "district")
    private String district;
}
