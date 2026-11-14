
package com.loanengine.application.scoring.controller;

import com.loanengine.application.entity.CreditProfile;
import com.loanengine.application.scoring.CreditScoreResponse;
import com.loanengine.application.scoring.CreditScoreResult;
import com.loanengine.application.scoring.service.CreditScoringService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/credit-scores")
public class CreditScoringController {

    private final CreditScoringService creditScoringService;

    public CreditScoringController(
            CreditScoringService creditScoringService) {

        this.creditScoringService = creditScoringService;
    }

    // Calculate credit score using customer's latest credit profile
    @PostMapping("/customer/{customerId}")
    public ResponseEntity<CreditScoreResponse> calculateCreditScore(
            @PathVariable Long customerId) {

        // Retrieve customer's latest credit profile
        CreditProfile profile =
                creditScoringService.getLatestCreditProfile(customerId);

        // Calculate score using the same profile
        CreditScoreResult result =
                creditScoringService.calculateCreditScore(profile);

        // Prepare API response
        CreditScoreResponse response = new CreditScoreResponse(
                customerId,
                profile.getId(),
                result.creditScore(),
                result.riskCategory(),
                result.paymentHistoryScore(),
                result.creditUtilizationScore(),
                result.debtToIncomeScore(),
                result.creditHistoryScore(),
                result.creditMixScore(),
                result.modelVersion()
        );

        return ResponseEntity.ok(response);
    }
}
