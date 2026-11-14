
package com.loanengine.application.eligibility.controller;

import com.loanengine.application.eligibility.model.EligibilityResult;
import com.loanengine.application.service.LoanEligibilityService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/loan-eligibility")
public class LoanEligibilityController {

    private final LoanEligibilityService loanEligibilityService;

    public LoanEligibilityController(
            LoanEligibilityService loanEligibilityService) {
        this.loanEligibilityService = loanEligibilityService;
    }

    @PostMapping("/{loanApplicationId}/evaluate")
    public ResponseEntity<EligibilityResult> evaluateLoanEligibility(
            @PathVariable Long loanApplicationId) {

        EligibilityResult result =
                loanEligibilityService.evaluateLoanEligibility(
                        loanApplicationId
                );

        return ResponseEntity.ok(result);
    }
}
