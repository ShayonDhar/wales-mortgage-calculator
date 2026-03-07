package com.dhar.propertymortgageapi.repository;

import com.dhar.propertymortgageapi.model.PropertyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PropertyTransactionRepository extends JpaRepository<PropertyTransaction, String> {

    @Query("SELECT AVG(p.price) FROM PropertyTransaction p WHERE p.postcode LIKE CONCAT(?1, '%')")
    Optional<BigDecimal> getAveragePriceByPostcodeArea(String postcodeArea);

    @Query("SELECT AVG(p.price) FROM PropertyTransaction p WHERE p.postcode LIKE CONCAT(?1, '%') AND p.propertyType = ?2")
    Optional<BigDecimal> getAveragePriceByPostcodeAndType(String postcodeArea, String propertyType);

    @Query("SELECT COUNT(p) FROM PropertyTransaction p WHERE p.postcode LIKE CONCAT(?1, '%')")
    long countTransactionsInArea(String postcodeArea);

    @Query("SELECT MAX(p.transferDate) FROM PropertyTransaction p")
    Optional<java.time.LocalDateTime> getLatestTransactionDate();

    List<PropertyTransaction> findByPostcode(String postcode);

    List<PropertyTransaction> findByTownCity(String townCity);
}
