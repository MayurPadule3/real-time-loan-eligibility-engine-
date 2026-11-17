
package com.loanengine.application.service;

import com.loanengine.application.scoring.CreditScoreResult;
import com.loanengine.application.scoring.service.CreditScoringService;

import com.loanengine.application.eligibility.model.EligibilityResult;
import com.loanengine.application.eligibility.rules.EligibilityRuleEvaluator;

import com.loanengine.application.entity.CreditProfile;
import com.loanengine.application.entity.LoanApplication;
import com.loanengine.application.entity.LoanStatus;

import com.loanengine.application.exception.ResourceNotFoundException;
import com.loanengine.application.repository.LoanApplicationRepository;

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

    /**
     * Evaluates the eligibility of a loan application.
     *
     * This method:
     * 1. Retrieves the loan application.
     * 2. Validates its current status.
     * 3. Retrieves the customer's latest credit profile.
     * 4. Calculates the credit score when sufficient payment data exists.
     * 5. Evaluates the application against eligibility rules.
     *
     * This operation does not update the loan application status
     * or persist the eligibility decision.
     */
    @Transactional(readOnly = true)
    public EligibilityResult evaluateLoanEligibility(
            Long loanApplicationId) {

        // Validate loan application ID.
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

        // Step 2: Validate application status.
        LoanStatus status = loanApplication.getStatus();

        if (status != LoanStatus.PENDING
                && status != LoanStatus.UNDER_REVIEW) {

            throw new IllegalStateException(
                    "Loan application cannot be evaluated because its status is: "
                            + status
            );
        }

        // Step 3: Retrieve customer ID.
        Long customerId = loanApplication.getCustomer().getId();

        // Step 4: Retrieve the latest credit profile.
        CreditProfile creditProfile =
                creditProfileService.getLatestCreditProfile(customerId);

        // Step 5: Calculate credit score when payment history
        // contains sufficient data.
        CreditScoreResult creditScoreResult = null;

        if (hasSufficientPaymentHistory(creditProfile)) {

            creditScoreResult =
                    creditScoringService.calculateCreditScore(
                            creditProfile
                    );
        }

        // Step 6: Evaluate loan eligibility.
        return eligibilityRuleEvaluator.evaluate(
                loanApplication,
                creditProfile,
                creditScoreResult
        );
    }

    /**
     * Checks whether sufficient payment history exists
     * for credit score calculation.
     */
    private boolean hasSufficientPaymentHistory(
            CreditProfile profile) {

        if (profile == null) {
            return false;
        }

        Integer totalPayments =
                profile.getTotalPaymentsLast12Months();

        Integer onTimePayments =
                profile.getOnTimePaymentsLast12Months();

        return totalPayments != null
                && totalPayments > 0
                && onTimePayments != null
                && onTimePayments >= 0
                && onTimePayments <= totalPayments;
    }
}
