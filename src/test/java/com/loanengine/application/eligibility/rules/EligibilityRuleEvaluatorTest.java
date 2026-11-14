
package com.loanengine.application.eligibility.rules;

import com.loanengine.application.entity.CreditDataSource;
import com.loanengine.application.entity.CreditProfile;
import com.loanengine.application.entity.EmploymentType;
import com.loanengine.application.entity.LoanApplication;

import com.loanengine.application.eligibility.model.EligibilityDecision;
import com.loanengine.application.eligibility.model.EligibilityReason;
import com.loanengine.application.eligibility.model.EligibilityResult;

import com.loanengine.application.scoring.CreditRiskCategory;
import com.loanengine.application.scoring.CreditScoreResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EligibilityRuleEvaluatorTest {

    @Mock
    private LoanEmiCalculator loanEmiCalculator;

    @InjectMocks
    private EligibilityRuleEvaluator eligibilityRuleEvaluator;

    private LoanApplication loanApplication;
    private CreditProfile creditProfile;
    private CreditScoreResult creditScoreResult;

    @BeforeEach
    void setUp() {

        loanApplication = new LoanApplication(
                null,
                new BigDecimal("500000"),
                new BigDecimal("12"),
                60,
                "HOME_PURCHASE"
        );

        creditProfile = new CreditProfile(
                null,
                new BigDecimal("100000"),
                new BigDecimal("10000"),
                EmploymentType.SALARIED,
                new BigDecimal("100000"),
                new BigDecimal("20000"),
                12,
                11,
                1,
                2,
                48,
                CreditDataSource.SYNTHETIC_DEMO,
                LocalDate.of(2026, 11, 14)
        );

        creditScoreResult = new CreditScoreResult(
                800,
                CreditRiskCategory.LOW,
                new BigDecimal("91.67"),
                new BigDecimal("80"),
                new BigDecimal("85"),
                new BigDecimal("55"),
                new BigDecimal("100"),
                "1.0"
        );
    }

    @Test
    void shouldApproveEligibleApplicant() {

        when(loanEmiCalculator.calculateMonthlyEmi(
                any(), any(), anyInt()))
                .thenReturn(new BigDecimal("20000"));

        EligibilityResult result =
                eligibilityRuleEvaluator.evaluate(
                        loanApplication,
                        creditProfile,
                        creditScoreResult
                );

        assertEquals(EligibilityDecision.APPROVED, result.decision());

        assertTrue(
                result.reasons().contains(EligibilityReason.ELIGIBLE)
        );

        assertEquals(
                new BigDecimal("30.00"),
                result.debtToIncomeRatio()
        );

        assertEquals(800, result.creditScore());

        assertEquals(CreditRiskCategory.LOW, result.creditRiskCategory());

        assertEquals("1.0", result.ruleVersion());
    }

    @Test
    void shouldRejectApplicantWithInsufficientIncome() {

        creditProfile = createCreditProfile(
                "20000", "0", 48
        );

        when(loanEmiCalculator.calculateMonthlyEmi(
                any(), any(), anyInt()))
                .thenReturn(new BigDecimal("1000"));

        EligibilityResult result =
                eligibilityRuleEvaluator.evaluate(
                        loanApplication,
                        creditProfile,
                        creditScoreResult
                );

        assertEquals(EligibilityDecision.REJECTED, result.decision());

        assertTrue(
                result.reasons().contains(
                        EligibilityReason.INSUFFICIENT_INCOME
                )
        );
    }

    @Test
    void shouldRejectApplicantWhenDebtToIncomeRatioExceeds50Percent() {

        when(loanEmiCalculator.calculateMonthlyEmi(
                any(), any(), anyInt()))
                .thenReturn(new BigDecimal("45000"));

        EligibilityResult result =
                eligibilityRuleEvaluator.evaluate(
                        loanApplication,
                        creditProfile,
                        creditScoreResult
                );

        assertEquals(EligibilityDecision.REJECTED, result.decision());

        assertEquals(
                new BigDecimal("55.00"),
                result.debtToIncomeRatio()
        );

        assertTrue(
                result.reasons().contains(
                        EligibilityReason.HIGH_DEBT_TO_INCOME_RATIO
                )
        );
    }

    @Test
    void shouldRequireManualReviewWhenDtiIsBetween40And50Percent() {

        when(loanEmiCalculator.calculateMonthlyEmi(
                any(), any(), anyInt()))
                .thenReturn(new BigDecimal("35000"));

        EligibilityResult result =
                eligibilityRuleEvaluator.evaluate(
                        loanApplication,
                        creditProfile,
                        creditScoreResult
                );

        assertEquals(
                EligibilityDecision.MANUAL_REVIEW,
                result.decision()
        );

        assertEquals(
                new BigDecimal("45.00"),
                result.debtToIncomeRatio()
        );

        assertTrue(
                result.reasons().contains(
                        EligibilityReason.ADDITIONAL_REVIEW_REQUIRED
                )
        );
    }

    @Test
    void shouldRejectApplicantWithCreditScoreBelow600() {

        CreditScoreResult lowScoreResult = createCreditScoreResult(
                550,
                CreditRiskCategory.HIGH
        );

        when(loanEmiCalculator.calculateMonthlyEmi(
                any(), any(), anyInt()))
                .thenReturn(new BigDecimal("20000"));

        EligibilityResult result =
                eligibilityRuleEvaluator.evaluate(
                        loanApplication,
                        creditProfile,
                        lowScoreResult
                );

        assertEquals(EligibilityDecision.REJECTED, result.decision());

        assertTrue(
                result.reasons().contains(
                        EligibilityReason.LOW_CREDIT_SCORE
                )
        );
    }

    @Test
    void shouldRequireManualReviewForCreditScoreBetween600And649() {

        CreditScoreResult mediumScoreResult = createCreditScoreResult(
                630,
                CreditRiskCategory.HIGH
        );

        when(loanEmiCalculator.calculateMonthlyEmi(
                any(), any(), anyInt()))
                .thenReturn(new BigDecimal("20000"));

        EligibilityResult result =
                eligibilityRuleEvaluator.evaluate(
                        loanApplication,
                        creditProfile,
                        mediumScoreResult
                );

        assertEquals(
                EligibilityDecision.MANUAL_REVIEW,
                result.decision()
        );

        assertTrue(
                result.reasons().contains(
                        EligibilityReason.ADDITIONAL_REVIEW_REQUIRED
                )
        );
    }

    @Test
    void shouldRequireManualReviewForHighCreditRisk() {

        CreditScoreResult highRiskResult = createCreditScoreResult(
                700,
                CreditRiskCategory.HIGH
        );

        when(loanEmiCalculator.calculateMonthlyEmi(
                any(), any(), anyInt()))
                .thenReturn(new BigDecimal("20000"));

        EligibilityResult result =
                eligibilityRuleEvaluator.evaluate(
                        loanApplication,
                        creditProfile,
                        highRiskResult
                );

        assertEquals(
                EligibilityDecision.MANUAL_REVIEW,
                result.decision()
        );

        assertTrue(
                result.reasons().contains(
                        EligibilityReason.HIGH_CREDIT_RISK
                )
        );
    }

    @Test
    void shouldRequireManualReviewForInsufficientCreditHistory() {

        creditProfile = createCreditProfile(
                "100000", "10000", 6
        );

        when(loanEmiCalculator.calculateMonthlyEmi(
                any(), any(), anyInt()))
                .thenReturn(new BigDecimal("20000"));

        EligibilityResult result =
                eligibilityRuleEvaluator.evaluate(
                        loanApplication,
                        creditProfile,
                        creditScoreResult
                );

        assertEquals(
                EligibilityDecision.MANUAL_REVIEW,
                result.decision()
        );

        assertTrue(
                result.reasons().contains(
                        EligibilityReason.INSUFFICIENT_CREDIT_HISTORY
                )
        );
    }

    @Test
    void shouldRequireManualReviewWhenCreditScoreDataIsMissing() {

        when(loanEmiCalculator.calculateMonthlyEmi(
                any(), any(), anyInt()))
                .thenReturn(new BigDecimal("20000"));

        EligibilityResult result =
                eligibilityRuleEvaluator.evaluate(
                        loanApplication,
                        creditProfile,
                        null
                );

        assertEquals(
                EligibilityDecision.MANUAL_REVIEW,
                result.decision()
        );

        assertTrue(
                result.reasons().contains(
                        EligibilityReason.INSUFFICIENT_CREDIT_DATA
                )
        );

        assertNull(result.creditScore());
        assertNull(result.creditRiskCategory());
    }

    @Test
    void shouldPrioritizeRejectionOverManualReview() {

        creditProfile = createCreditProfile(
                "20000", "10000", 48
        );

        CreditScoreResult lowScoreResult = createCreditScoreResult(
                630,
                CreditRiskCategory.HIGH
        );

        when(loanEmiCalculator.calculateMonthlyEmi(
                any(), any(), anyInt()))
                .thenReturn(new BigDecimal("10000"));

        EligibilityResult result =
                eligibilityRuleEvaluator.evaluate(
                        loanApplication,
                        creditProfile,
                        lowScoreResult
                );

        assertEquals(EligibilityDecision.REJECTED, result.decision());

        assertTrue(
                result.reasons().contains(
                        EligibilityReason.INSUFFICIENT_INCOME
                )
        );

        assertTrue(
                result.reasons().contains(
                        EligibilityReason.ADDITIONAL_REVIEW_REQUIRED
                )
        );
    }

    @Test
    void shouldRejectNullLoanApplication() {

        assertThrows(
                IllegalArgumentException.class,
                () -> eligibilityRuleEvaluator.evaluate(
                        null,
                        creditProfile,
                        creditScoreResult
                )
        );
    }

    @Test
    void shouldRejectNullCreditProfile() {

        assertThrows(
                IllegalArgumentException.class,
                () -> eligibilityRuleEvaluator.evaluate(
                        loanApplication,
                        null,
                        creditScoreResult
                )
        );
    }

    private CreditProfile createCreditProfile(
            String monthlyIncome,
            String monthlyDebt,
            int creditHistoryMonths) {

        return new CreditProfile(
                null,
                new BigDecimal(monthlyIncome),
                new BigDecimal(monthlyDebt),
                EmploymentType.SALARIED,
                new BigDecimal("100000"),
                new BigDecimal("20000"),
                12,
                11,
                1,
                2,
                creditHistoryMonths,
                CreditDataSource.SYNTHETIC_DEMO,
                LocalDate.of(2026, 11, 14)
        );
    }

    private CreditScoreResult createCreditScoreResult(
            int score,
            CreditRiskCategory riskCategory) {

        return new CreditScoreResult(
                score,
                riskCategory,
                new BigDecimal("80"),
                new BigDecimal("70"),
                new BigDecimal("75"),
                new BigDecimal("60"),
                new BigDecimal("80"),
                "1.0"
        );
    }
}
