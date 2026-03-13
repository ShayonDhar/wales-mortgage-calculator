package com.dhar.propertymortgageapi.controller;

import com.dhar.propertymortgageapi.dto.AffordabilityRequestDto;
import com.dhar.propertymortgageapi.dto.AffordabilityResponseDto;
import com.dhar.propertymortgageapi.service.MortgageCalculatorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for handling mortgage affordability requests.
 */
@RestController
@RequestMapping("/api/v1/mortgages")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MortgageController {

    private final MortgageCalculatorService mortgageCalculatorService;

    /**
     * Endpoint to calculate mortgage affordability and return a detailed response.
     *
     * @param request the validated affordability request from the client
     * @return a ResponseEntity containing the AffordabilityResponseDto
     */
    @PostMapping("/calculate")
    public ResponseEntity<AffordabilityResponseDto> calculateAffordability(
            @Valid @RequestBody AffordabilityRequestDto request) {

        AffordabilityResponseDto response =
                mortgageCalculatorService.calculateAffordability(request);
        return ResponseEntity.ok(response);
    }
}