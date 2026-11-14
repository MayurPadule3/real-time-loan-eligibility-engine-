
package com.loanengine.application.scoring;

import com.loanengine.application.entity.CreditProfile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class CreditScoreCalculator {

    private static final String MODEL_VERSION = "1.0";

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal HUNDRED =
            new BigDecimal("100");

    private static final BigDecimal PAYMENT_WEIGHT =
            new BigDecimal("0.35");

    private static final BigDecimal UTILIZATION_WEIGHT =
            new BigDecimal("0.25");

    private static final BigDecimal DEBT_TO_INCOME_WEIGHT =
            new BigDecimal("0.20");

    private static final BigDecimal CREDIT_HISTORY_WEIGHT =
            new BigDecimal("0.10");

    private static final BigDecimal CREDIT_MIX_WEIGHT =
            new BigDecimal("0.10");

    public CreditScoreResult calculate(CreditProfile profile) {

        validateProfile(profile);

        BigDecimal paymentScore =
                calculatePaymentHistoryScore(profile);

        BigDecimal utilizationScore =
                calculateCreditUtilizationScore(profile);

        BigDecimal debtToIncomeScore =
                calculateDebtToIncomeScore(profile);

        BigDecimal creditHistoryScore =
                calculateCreditHistoryScore(profile);

        BigDecimal creditMixScore =
                calculateCreditMixScore(profile);

        BigDecimal weightedScore =
                paymentScore.multiply(PAYMENT_WEIGHT)
                        .add(utilizationScore.multiply(UTILIZATION_WEIGHT))
                        .add(debtToIncomeScore.multiply(DEBT_TO_INCOME_WEIGHT))
                        .add(creditHistoryScore.multiply(CREDIT_HISTORY_WEIGHT))
                        .add(creditMixScore.multiply(CREDIT_MIX_WEIGHT));

        int finalScore = 300 + weightedScore
                .multiply(new BigDecimal("6"))
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();

        CreditRiskCategory riskCategory =
                determineRiskCategory(finalScore);

        return new CreditScoreResult(
                finalScore,
                riskCategory,
                paymentScore,
                utilizationScore,
                debtToIncomeScore,
                creditHistoryScore,
                creditMixScore,
                MODEL_VERSION
        );
    }

    // Calculate payment history score
    private BigDecimal calculatePaymentHistoryScore(
            CreditProfile profile) {

        int totalPayments =
                profile.getTotalPaymentsLast12Months();

        int onTimePayments =
                profile.getOnTimePaymentsLast12Months();

        if (totalPayments == 0) {
            throw new IllegalArgumentException(
                    "Insufficient payment history to calculate credit score."
            );
        }

        return BigDecimal.valueOf(onTimePayments)
                .multiply(HUNDRED)
                .divide(
                        BigDecimal.valueOf(totalPayments),
                        2,
                        RoundingMode.HALF_UP
                );
    }

    // Calculate credit utilization score
    private BigDecimal calculateCreditUtilizationScore(
            CreditProfile profile) {

        BigDecimal creditLimit =
                profile.getRevolvingCreditLimit();

        BigDecimal creditBalance =
                profile.getRevolvingCreditBalance();

        if (creditLimit.compareTo(ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Insufficient revolving credit data."
            );
        }

        BigDecimal utilization = creditBalance
                .multiply(HUNDRED)
                .divide(creditLimit, 2, RoundingMode.HALF_UP);

        if (utilization.compareTo(new BigDecimal("10")) <= 0) {
            return new BigDecimal("100");
        } else if (utilization.compareTo(new BigDecimal("30")) <= 0) {
            return new BigDecimal("80");
        } else if (utilization.compareTo(new BigDecimal("50")) <= 0) {
            return new BigDecimal("60");
        } else if (utilization.compareTo(new BigDecimal("75")) <= 0) {
            return new BigDecimal("40");
        } else if (utilization.compareTo(HUNDRED) <= 0) {
            return new BigDecimal("20");
        }

        return ZERO;
    }

    // Calculate debt-to-income score
    private BigDecimal calculateDebtToIncomeScore(
            CreditProfile profile) {

        BigDecimal income = profile.getMonthlyIncome();

        BigDecimal monthlyDebt =
                profile.getMonthlyDebtObligations();

        BigDecimal ratio = monthlyDebt
                .multiply(HUNDRED)
                .divide(income, 2, RoundingMode.HALF_UP);

        if (ratio.compareTo(new BigDecimal("20")) <= 0) {
            return new BigDecimal("100");
        } else if (ratio.compareTo(new BigDecimal("30")) <= 0) {
            return new BigDecimal("85");
        } else if (ratio.compareTo(new BigDecimal("40")) <= 0) {
            return new BigDecimal("70");
        } else if (ratio.compareTo(new BigDecimal("50")) <= 0) {
            return new BigDecimal("50");
        } else if (ratio.compareTo(new BigDecimal("60")) <= 0) {
            return new BigDecimal("25");
        }

        return ZERO;
    }

    // Calculate credit history score
    private BigDecimal calculateCreditHistoryScore(
            CreditProfile profile) {

        int months = profile.getCreditHistoryMonths();

        if (months >= 120) {
            return new BigDecimal("100");
        } else if (months >= 84) {
            return new BigDecimal("85");
        } else if (months >= 60) {
            return new BigDecimal("70");
        } else if (months >= 36) {
            return new BigDecimal("55");
        } else if (months >= 12) {
            return new BigDecimal("35");
        }

        return new BigDecimal("15");
    }

    // Calculate credit mix score
    private BigDecimal calculateCreditMixScore(
            CreditProfile profile) {

        int secured =
                profile.getSecuredCreditAccounts();

        int unsecured =
                profile.getUnsecuredCreditAccounts();

        if (secured > 0 && unsecured > 0) {
            return new BigDecimal("100");
        } else if (secured > 0 || unsecured > 0) {
            return new BigDecimal("70");
        }

        return new BigDecimal("50");
    }

    // Determine risk category
    private CreditRiskCategory determineRiskCategory(int score) {

        if (score >= 750) {
            return CreditRiskCategory.LOW;
        } else if (score >= 650) {
            return CreditRiskCategory.MEDIUM;
        }

        return CreditRiskCategory.HIGH;
    }

    // Validate credit profile before calculation
    private void validateProfile(CreditProfile profile) {

        if (profile == null) {
            throw new IllegalArgumentException(
                    "Credit profile is required."
            );
        }

        if (profile.getMonthlyIncome() == null
                || profile.getMonthlyIncome().compareTo(ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Monthly income must be greater than zero."
            );
        }

        if (profile.getMonthlyDebtObligations() == null
                || profile.getMonthlyDebtObligations().compareTo(ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Monthly debt obligations cannot be negative."
            );
        }

        if (profile.getRevolvingCreditLimit() == null
                || profile.getRevolvingCreditLimit().compareTo(ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Revolving credit limit must be greater than zero."
            );
        }

        if (profile.getRevolvingCreditBalance() == null
                || profile.getRevolvingCreditBalance().compareTo(ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Revolving credit balance cannot be negative."
            );
        }

        if (profile.getTotalPaymentsLast12Months() == null
                || profile.getOnTimePaymentsLast12Months() == null
                || profile.getTotalPaymentsLast12Months() < 0
                || profile.getOnTimePaymentsLast12Months() < 0) {
            throw new IllegalArgumentException(
                    "Valid payment history values are required."
            );
        }

        if (profile.getOnTimePaymentsLast12Months()
                > profile.getTotalPaymentsLast12Months()) {
            throw new IllegalArgumentException(
                    "On-time payments cannot exceed total payments."
            );
        }

        if (profile.getSecuredCreditAccounts() == null
                || profile.getUnsecuredCreditAccounts() == null
                || profile.getSecuredCreditAccounts() < 0
                || profile.getUnsecuredCreditAccounts() < 0) {
            throw new IllegalArgumentException(
                    "Valid credit account counts are required."
            );
        }

        if (profile.getCreditHistoryMonths() == null
                || profile.getCreditHistoryMonths() < 0) {
            throw new IllegalArgumentException(
                    "Credit history months cannot be negative."
            );
        }
    }
}
