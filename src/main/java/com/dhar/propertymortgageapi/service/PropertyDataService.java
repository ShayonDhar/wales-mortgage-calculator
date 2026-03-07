package com.dhar.propertymortgageapi.service;

import com.dhar.propertymortgageapi.repository.PropertyTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PropertyDataService {

    private final PropertyTransactionRepository propertyTransactionRepository;

    /**
     * Returns the average price of mortgages based on postcode area/property type
     * @param postcodeArea The area of the postcode
     * @param propertyType The type of property
     * @return Average price of buildings in the provided area
     */
    public BigDecimal getAveragePrice(String postcodeArea, String propertyType) {
        if (propertyType != null && !propertyType.isEmpty()) {
            // If the user selected a specific type like Flats
            return propertyTransactionRepository.getAveragePriceByPostcodeAndType(postcodeArea, propertyType)
                    .orElse(BigDecimal.ZERO);
        }
        // otherwise, get the general average for the area
        return propertyTransactionRepository.getAveragePriceByPostcodeArea(postcodeArea)
                .orElse(BigDecimal.ZERO);
    }

    public long getTransactionCount(String postcodeArea) {
        return propertyTransactionRepository.countTransactionsInArea(postcodeArea);
    }

}
