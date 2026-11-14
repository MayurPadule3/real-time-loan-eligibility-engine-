
package com.loanengine.application.eligibility.rules;

import com.loanengine.application.entity.CreditProfile;
import com.loanengine.application.entity.LoanApplication;
import com.loanengine.application.scoring.CreditScoreResult;
import com.loanengine.application.eligibility.model.EligibilityDecision;
import com.loanengine.application.eligibility.model.EligibilityReason;
import com.loanengine.application.eligibility.model.EligibilityResult;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Component
public class EligibilityRuleEvaluator {

    private static final BigDecimal MINIMUM_MONTHLY_INCOME =
            new BigDecimal("25000");

    private static final BigDecimal MANUAL_REVIEW_DTI =
            new BigDecimal("40");

    private static final BigDecimal MAXIMUM_DTI =
            new BigDecimal("50");

    private static final int MINIMUM_CREDIT_SCORE = 600;

    private static final int MANUAL_REVIEW_CREDIT_SCORE = 650;

    private static final int MINIMUM_CREDIT_HISTORY_MONTHS = 12;

    private static final String RULE_VERSION = "1.0";

    private final LoanEmiCalculator loanEmiCalculator;

    public EligibilityRuleEvaluator(
            LoanEmiCalculator loanEmiCalculator) {

        this.loanEmiCalculator = loanEmiCalculator;
    }

    public EligibilityResult evaluate(
            LoanApplication loanApplication,
            CreditProfile creditProfile,
            CreditScoreResult creditScoreResult) {

        validateRequiredInputs(loanApplication, creditProfile);

        List<EligibilityReason> reasons = new ArrayList<>();

        BigDecimal monthlyIncome = creditProfile.getMonthlyIncome();

        BigDecimal existingMonthlyDebt =
                creditProfile.getMonthlyDebtObligations();

        validateFinancialData(monthlyIncome, existingMonthlyDebt);

        // Rule 1: Minimum monthly income.

        if (monthlyIncome.compareTo(MINIMUM_MONTHLY_INCOME) < 0) {

            reasons.add(EligibilityReason.INSUFFICIENT_INCOME);
        }

        // Rule 2: Calculate proposed loan EMI.

        BigDecimal monthlyEmi =
                loanEmiCalculator.calculateMonthlyEmi(
                        loanApplication.getLoanAmount(),
                        loanApplication.getInterestRate(),
                        loanApplication.getTenureMonths()
                );

        // Rule 3: Calculate debt-to-income ratio.

        BigDecimal totalMonthlyObligations =
                existingMonthlyDebt.add(monthlyEmi);

        BigDecimal debtToIncomeRatio =
                totalMonthlyObligations
                        .multiply(new BigDecimal("100"))
                        .divide(
                                monthlyIncome,
                                2,
                                RoundingMode.HALF_UP
                        );

        if (debtToIncomeRatio.compareTo(MAXIMUM_DTI) > 0) {

            reasons.add(
                    EligibilityReason.HIGH_DEBT_TO_INCOME_RATIO
            );

        } else if (
                debtToIncomeRatio.compareTo(MANUAL_REVIEW_DTI) > 0) {

            reasons.add(
                    EligibilityReason.ADDITIONAL_REVIEW_REQUIRED
            );
        }

        // Rule 4: Check credit data availability.

        boolean insufficientCreditData =
                creditScoreResult == null
                        || creditProfile.getTotalPaymentsLast12Months() == null
                        || creditProfile.getTotalPaymentsLast12Months() <= 0
                        || creditProfile.getOnTimePaymentsLast12Months() == null
                        || creditProfile.getCreditHistoryMonths() == null;

        if (insufficientCreditData) {

            reasons.add(
                    EligibilityReason.INSUFFICIENT_CREDIT_DATA
            );

        } else {

            // Rule 5: Minimum credit score.

            int creditScore = creditScoreResult.creditScore();

            if (creditScore < MINIMUM_CREDIT_SCORE) {

                reasons.add(
                        EligibilityReason.LOW_CREDIT_SCORE
                );

            } else if (
                    creditScore < MANUAL_REVIEW_CREDIT_SCORE) {

                reasons.add(
                        EligibilityReason.ADDITIONAL_REVIEW_REQUIRED
                );
            }

            // Rule 6: Credit risk category.

            if (creditScoreResult.riskCategory() == null) {

                reasons.add(
                        EligibilityReason.INSUFFICIENT_CREDIT_DATA
                );

            } else if (
                    creditScoreResult.riskCategory()
                            == com.loanengine.application.scoring.CreditRiskCategory.HIGH) {

                reasons.add(
                        EligibilityReason.HIGH_CREDIT_RISK
                );
            }

            // Rule 7: Credit history length.

            if (creditProfile.getCreditHistoryMonths()
                    < MINIMUM_CREDIT_HISTORY_MONTHS) {

                reasons.add(
                        EligibilityReason.INSUFFICIENT_CREDIT_HISTORY
                );
            }
        }

        // Determine the final decision.
        // Rejection takes priority over manual review.

        EligibilityDecision decision;

        if (containsRejectionReason(reasons)) {

            decision = EligibilityDecision.REJECTED;

        } else if (containsManualReviewReason(reasons)) {

            decision = EligibilityDecision.MANUAL_REVIEW;

        } else {

            decision = EligibilityDecision.APPROVED;

            reasons.add(EligibilityReason.ELIGIBLE);
        }

        return new EligibilityResult(
                decision,
                reasons,
                debtToIncomeRatio,
                creditScoreResult == null
                        ? null
                        : creditScoreResult.creditScore(),
                creditScoreResult == null
                        ? null
                        : creditScoreResult.riskCategory(),
                RULE_VERSION
        );
    }

    private void validateRequiredInputs(
            LoanApplication loanApplication,
            CreditProfile creditProfile) {

        if (loanApplication == null) {

            throw new IllegalArgumentException(
                    "Loan application cannot be null"
            );
        }

        if (creditProfile == null) {

            throw new IllegalArgumentException(
                    "Credit profile cannot be null"
            );
        }
    }

    private void validateFinancialData(
            BigDecimal monthlyIncome,
            BigDecimal existingMonthlyDebt) {

        if (monthlyIncome == null
                || monthlyIncome.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Monthly income must be greater than zero"
            );
        }

        if (existingMonthlyDebt == null
                || existingMonthlyDebt.compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "Monthly debt obligations cannot be negative or null"
            );
        }
    }

    private boolean containsRejectionReason(
            List<EligibilityReason> reasons) {

        return reasons.contains(
                EligibilityReason.INSUFFICIENT_INCOME
        ) || reasons.contains(
                EligibilityReason.HIGH_DEBT_TO_INCOME_RATIO
        ) || reasons.contains(
                EligibilityReason.LOW_CREDIT_SCORE
        );
    }

    private boolean containsManualReviewReason(
            List<EligibilityReason> reasons) {

        return reasons.contains(
                EligibilityReason.ADDITIONAL_REVIEW_REQUIRED
        ) || reasons.contains(
                EligibilityReason.HIGH_CREDIT_RISK
        ) || reasons.contains(
                EligibilityReason.INSUFFICIENT_CREDIT_DATA
        ) || reasons.contains(
                EligibilityReason.INSUFFICIENT_CREDIT_HISTORY
        );
    }
}
