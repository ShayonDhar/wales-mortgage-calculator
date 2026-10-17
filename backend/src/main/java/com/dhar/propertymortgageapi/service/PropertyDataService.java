package com.dhar.propertymortgageapi.service;

import com.dhar.propertymortgageapi.repository.PropertyTransactionRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Service for retrieving property transaction data.
 */
@Service
@RequiredArgsConstructor
public class PropertyDataService {

    private static final int PRICE_SCALE = 2;

    private final PropertyTransactionRepository propertyTransactionRepository;

    /**
     * Returns the average sale price for a postcode area, optionally filtered by property type.
     *
     * @param postcodeArea the postcode area prefix, e.g. "CF"
     * @param propertyType the property type code, or null/empty for all types
     * @return the average sale price, or zero if there are no matching transactions
     */
    @Cacheable(value = "averagePrices", key = "#postcodeArea + '-' + #propertyType")
    public BigDecimal getAveragePrice(String postcodeArea, String propertyType) {
        if (propertyType != null && !propertyType.isEmpty()) {
            return propertyTransactionRepository
                    .getAveragePriceByPostcodeAndType(postcodeArea, propertyType)
                    .orElse(BigDecimal.ZERO);
        }

        return propertyTransactionRepository.getAveragePriceByPostcodeArea(postcodeArea)
                .orElse(BigDecimal.ZERO);
    }

    /**
     * Gets the total number of transactions in a given postcode area.
     *
     * @param postcodeArea the postcode area prefix to count
     * @return the total number of transactions
     */
    public long getTransactionCount(String postcodeArea) {
        return propertyTransactionRepository.countTransactionsInArea(postcodeArea);
    }

    /**
     * Returns average sale prices broken down by property type for a postcode area.
     *
     * @param postcodeArea the postcode area prefix
     * @return a map of property type code to average sale price
     */
    @Cacheable(value = "areaBreakdowns", key = "#postcodeArea")
    public Map<String, BigDecimal> getAreaPriceBreakdown(String postcodeArea) {
        List<Object[]> results = propertyTransactionRepository
                .getAveragePricesGroupedByType(postcodeArea);
        Map<String, BigDecimal> breakdown = new HashMap<>();

        for (Object[] row : results) {
            if (row[0] instanceof String type && row[1] instanceof Number averagePrice) {
                breakdown.put(type, new BigDecimal(averagePrice.toString())
                        .setScale(PRICE_SCALE, RoundingMode.HALF_UP));
            }
        }

        return breakdown;
    }
}
