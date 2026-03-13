package com.dhar.propertymortgageapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * Entity representing a property transaction in the database.
 */
@Entity
@Table(name = "property_transactions")
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