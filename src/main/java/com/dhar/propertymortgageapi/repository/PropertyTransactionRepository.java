package com.dhar.propertymortgageapi.repository;

import com.dhar.propertymortgageapi.model.PropertyTransaction;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for accessing property transaction data.
 */
@Repository
public interface PropertyTransactionRepository extends JpaRepository<PropertyTransaction, String> {

    /**
     * Gets the average property price by postcode area.
     *
     * @param postcodeArea the postcode area to search
     * @return an Optional containing the average price if found
     */
    @Query("SELECT AVG(p.price) FROM PropertyTransaction p WHERE p.postcode LIKE CONCAT(?1, '%')")
    Optional<BigDecimal> getAveragePriceByPostcodeArea(String postcodeArea);

    /**
     * Gets the average property price by postcode area and property type.
     *
     * @param postcodeArea the postcode area to search
     * @param propertyType the type of property
     * @return an Optional containing the average price if found
     */
    @Query("SELECT AVG(p.price) FROM PropertyTransaction p "
            + "WHERE p.postcode LIKE CONCAT(?1, '%') AND p.propertyType = ?2")
    Optional<BigDecimal> getAveragePriceByPostcodeAndType(
            String postcodeArea, String propertyType);

    /**
     * Counts the total number of transactions in a given postcode area.
     *
     * @param postcodeArea the postcode area to search
     * @return the total number of transactions
     */
    @Query("SELECT COUNT(p) FROM PropertyTransaction p WHERE p.postcode LIKE CONCAT(?1, '%')")
    long countTransactionsInArea(String postcodeArea);

    /**
     * Retrieves the date of the most recent transaction.
     *
     * @return an Optional containing the latest transaction date
     */
    @Query("SELECT MAX(p.transferDate) FROM PropertyTransaction p")
    Optional<LocalDateTime> getLatestTransactionDate();

    /**
     * Finds a list of transactions by exact postcode.
     *
     * @param postcode the full postcode to search
     * @return a list of matching transactions
     */
    List<PropertyTransaction> findByPostcode(String postcode);

    /**
     * Finds a list of transactions by town or city.
     *
     * @param townCity the town or city name
     * @return a list of matching transactions
     */
    List<PropertyTransaction> findByTownCity(String townCity);
}