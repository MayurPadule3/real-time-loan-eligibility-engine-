
package com.loanengine.application.controller;

import com.loanengine.application.dto.LoanApplicationRequest;
import com.loanengine.application.dto.LoanApplicationResponse;
import com.loanengine.application.entity.LoanStatus;
import com.loanengine.application.service.LoanApplicationService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/loan-applications")
public class LoanApplicationController {

    private final LoanApplicationService loanApplicationService;

    public LoanApplicationController(
            LoanApplicationService loanApplicationService
    ) {
        this.loanApplicationService = loanApplicationService;
    }

    // Submit a new loan application
    @PostMapping
    public ResponseEntity<LoanApplicationResponse> createLoanApplication(
            @Valid @RequestBody LoanApplicationRequest request
    ) {
        LoanApplicationResponse response =
                loanApplicationService.createLoanApplication(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Get all loan applications
    @GetMapping
    public ResponseEntity<List<LoanApplicationResponse>> getAllLoanApplications() {

        return ResponseEntity.ok(
                loanApplicationService.getAllLoanApplications()
        );
    }

    // Get loan application by ID
    @GetMapping("/{id}")
    public ResponseEntity<LoanApplicationResponse> getLoanApplicationById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                loanApplicationService.getLoanApplicationById(id)
        );
    }

    // Get loan applications by customer ID
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<LoanApplicationResponse>>
    getLoanApplicationsByCustomerId(
            @PathVariable Long customerId
    ) {
        return ResponseEntity.ok(
                loanApplicationService.getLoanApplicationsByCustomerId(customerId)
        );
    }

    // Get loan applications by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<LoanApplicationResponse>>
    getLoanApplicationsByStatus(
            @PathVariable LoanStatus status
    ) {
        return ResponseEntity.ok(
                loanApplicationService.getLoanApplicationsByStatus(status)
        );
    }
}
