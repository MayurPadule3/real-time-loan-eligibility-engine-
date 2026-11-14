
package com.loanengine.application.controller;

import com.loanengine.application.dto.CreditProfileRequest;
import com.loanengine.application.dto.CreditProfileResponse;
import com.loanengine.application.entity.CreditProfile;
import com.loanengine.application.service.CreditProfileService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/credit-profiles")
public class CreditProfileController {

    private final CreditProfileService creditProfileService;

    public CreditProfileController(
            CreditProfileService creditProfileService) {
        this.creditProfileService = creditProfileService;
    }

    // Create a new credit profile snapshot
    @PostMapping
    public ResponseEntity<CreditProfileResponse> createCreditProfile(
            @Valid @RequestBody CreditProfileRequest request) {

        CreditProfile profile =
                creditProfileService.createCreditProfile(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CreditProfileResponse.from(profile));
    }

    // Get a credit profile by ID
    @GetMapping("/{id}")
    public ResponseEntity<CreditProfileResponse> getCreditProfileById(
            @PathVariable Long id) {

        CreditProfile profile =
                creditProfileService.getCreditProfileById(id);

        return ResponseEntity.ok(
                CreditProfileResponse.from(profile)
        );
    }

    // Get all credit profile snapshots for a customer
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<CreditProfileResponse>>
    getCreditProfilesByCustomerId(@PathVariable Long customerId) {

        List<CreditProfileResponse> responses =
                creditProfileService
                        .getCreditProfilesByCustomerId(customerId)
                        .stream()
                        .map(CreditProfileResponse::from)
                        .toList();

        return ResponseEntity.ok(responses);
    }

    // Get the latest credit profile snapshot for a customer
    @GetMapping("/customer/{customerId}/latest")
    public ResponseEntity<CreditProfileResponse> getLatestCreditProfile(
            @PathVariable Long customerId) {

        CreditProfile profile =
                creditProfileService.getLatestCreditProfile(customerId);

        return ResponseEntity.ok(
                CreditProfileResponse.from(profile)
        );
    }
}
