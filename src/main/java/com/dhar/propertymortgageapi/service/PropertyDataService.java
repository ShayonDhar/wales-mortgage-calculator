package com.dhar.propertymortgageapi.service;

import com.dhar.propertymortgageapi.repository.PropertyTransactionRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Service for retrieving property transaction data.
 */
@Service
@RequiredArgsConstructor
public class PropertyDataService {

    private final PropertyTransactionRepository propertyTransactionRepository;

    /**
     * Returns the average price of mortgages based on postcode area/property type.
     *
     * @param postcodeArea The area of the postcode
     * @param propertyType The type of property
     * @return Average price of buildings in the provided area
     */
    @Cacheable(value = "averagePrices", key = "#postcodeArea + '-' + #propertyType")
    public BigDecimal getAveragePrice(String postcodeArea, String propertyType) {
        if (propertyType != null && !propertyType.isEmpty()) {
            // If the user selected a specific type like Flats
            return propertyTransactionRepository
                    .getAveragePriceByPostcodeAndType(postcodeArea, propertyType)
                    .orElse(BigDecimal.ZERO);
        }
        // otherwise, get the general average for the area
        return propertyTransactionRepository.getAveragePriceByPostcodeArea(postcodeArea)
                .orElse(BigDecimal.ZERO);
    }

    /**
     * Gets the total number of transactions in a given postcode area.
     *
     * @param postcodeArea The area of the postcode to count
     * @return The total number of transactions
     */
    public long getTransactionCount(String postcodeArea) {
        return propertyTransactionRepository.countTransactionsInArea(postcodeArea);
    }

    /**
     * Returns a map of average prices broken down by property type for a given area.
     * Useful for bar charts comparing prices across property types.
     */
    @Cacheable(value = "areaBreakdowns", key = "#postcodeArea")
    public java.util.Map<String, BigDecimal> getAreaPriceBreakdown(String postcodeArea) {
        java.util.List<Object[]> results = propertyTransactionRepository
                .getAveragePricesGroupedByType(postcodeArea);

        java.util.Map<String, BigDecimal> breakdown = new java.util.HashMap<>();

        for (Object[] row : results) {
            String type = (String) row[0];
            Number avgPriceRaw = (Number) row[1];

            if (type != null && avgPriceRaw != null) {
                BigDecimal avgPrice = new BigDecimal(avgPriceRaw.toString())
                        .setScale(2, java.math.RoundingMode.HALF_UP);
                breakdown.put(type, avgPrice);
            }
        }
        return breakdown;
    }
}