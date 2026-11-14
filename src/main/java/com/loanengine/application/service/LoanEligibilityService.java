
package com.loanengine.application.service;

import com.loanengine.application.scoring.CreditScoreResult;
import com.loanengine.application.scoring.service.CreditScoringService;

import com.loanengine.application.eligibility.model.EligibilityResult;
import com.loanengine.application.eligibility.rules.EligibilityRuleEvaluator;
import com.loanengine.application.entity.CreditProfile;
import com.loanengine.application.entity.LoanApplication;
import com.loanengine.application.exception.ResourceNotFoundException;
import com.loanengine.application.repository.LoanApplicationRepository;
import com.loanengine.application.scoring.CreditScoreResult;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoanEligibilityService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final CreditProfileService creditProfileService;
    private final CreditScoringService creditScoringService;
    private final EligibilityRuleEvaluator eligibilityRuleEvaluator;

    public LoanEligibilityService(
            LoanApplicationRepository loanApplicationRepository,
            CreditProfileService creditProfileService,
            CreditScoringService creditScoringService,
            EligibilityRuleEvaluator eligibilityRuleEvaluator) {

        this.loanApplicationRepository = loanApplicationRepository;
        this.creditProfileService = creditProfileService;
        this.creditScoringService = creditScoringService;
        this.eligibilityRuleEvaluator = eligibilityRuleEvaluator;
    }

    @Transactional(readOnly = true)
    public EligibilityResult evaluateLoanEligibility(Long loanApplicationId) {

        if (loanApplicationId == null || loanApplicationId <= 0) {
            throw new IllegalArgumentException(
                    "A valid loan application ID is required."
            );
        }

        // Step 1: Retrieve the loan application.
        LoanApplication loanApplication =
                loanApplicationRepository.findById(loanApplicationId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Loan application not found with ID: "
                                        + loanApplicationId
                        ));

        // Step 2: Retrieve the latest credit profile for the customer.
        Long customerId = loanApplication.getCustomer().getId();

        CreditProfile creditProfile =
                creditProfileService.getLatestCreditProfile(customerId);

        // Step 3: Calculate credit score only when payment history
        // contains sufficient data.
        CreditScoreResult creditScoreResult = null;

        if (hasSufficientPaymentHistory(creditProfile)) {
            creditScoreResult =
                    creditScoringService.calculateCreditScore(creditProfile);
        }

        // Step 4: Evaluate eligibility using the business rules.
        return eligibilityRuleEvaluator.evaluate(
                loanApplication,
                creditProfile,
                creditScoreResult
        );
    }

    private boolean hasSufficientPaymentHistory(CreditProfile profile) {

        Integer totalPayments = profile.getTotalPaymentsLast12Months();
        Integer onTimePayments = profile.getOnTimePaymentsLast12Months();

        return totalPayments != null
                && totalPayments > 0
                && onTimePayments != null
                && onTimePayments >= 0
                && onTimePayments <= totalPayments;
    }
}
