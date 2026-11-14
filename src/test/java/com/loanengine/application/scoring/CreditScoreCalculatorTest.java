
package com.loanengine.application.scoring;

import com.loanengine.application.entity.CreditDataSource;
import com.loanengine.application.entity.CreditProfile;
import com.loanengine.application.entity.EmploymentType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class CreditScoreCalculatorTest {

    private CreditScoreCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new CreditScoreCalculator();
    }

    private CreditProfile createProfile(
            String income,
            String debt,
            String limit,
            String balance,
            int totalPayments,
            int onTimePayments,
            int secured,
            int unsecured,
            int historyMonths) {

        return new CreditProfile(
                null,
                new BigDecimal(income),
                new BigDecimal(debt),
                EmploymentType.SALARIED,
                new BigDecimal(limit),
                new BigDecimal(balance),
                totalPayments,
                onTimePayments,
                secured,
                unsecured,
                historyMonths,
                CreditDataSource.SYNTHETIC_DEMO,
                LocalDate.of(2026, 11, 14)
        );
    }

    @Test
    void shouldCalculateExpectedCreditScore() {

        CreditProfile profile = createProfile(
                "65000", "15000",
                "100000", "25000",
                12, 11, 1, 2, 48
        );

        CreditScoreResult result = calculator.calculate(profile);

        assertEquals(808, result.creditScore());
        assertEquals(CreditRiskCategory.LOW, result.riskCategory());
        assertEquals("1.0", result.modelVersion());

        assertEquals(
                0,
                new BigDecimal("91.67")
                        .compareTo(result.paymentHistoryScore())
        );
    }

    @Test
    void shouldCalculateMaximumScore() {

        CreditProfile profile = createProfile(
                "100000", "10000",
                "100000", "5000",
                12, 12, 2, 2, 150
        );

        CreditScoreResult result = calculator.calculate(profile);

        assertEquals(900, result.creditScore());
        assertEquals(CreditRiskCategory.LOW, result.riskCategory());
    }

    @Test
    void shouldClassifyHighRiskProfile() {

        CreditProfile profile = createProfile(
                "30000", "25000",
                "100000", "95000",
                12, 4, 0, 1, 6
        );

        CreditScoreResult result = calculator.calculate(profile);

        assertEquals(CreditRiskCategory.HIGH, result.riskCategory());
        assertTrue(result.creditScore() < 650);
    }

    @Test
    void shouldClassifyMediumRiskProfile() {

        CreditProfile profile = createProfile(
                "50000", "20000",
                "100000", "40000",
                12, 10, 1, 0, 48
        );

        CreditScoreResult result = calculator.calculate(profile);

        assertEquals(CreditRiskCategory.MEDIUM, result.riskCategory());
        assertTrue(result.creditScore() >= 650);
        assertTrue(result.creditScore() < 750);
    }

    @Test
    void shouldRejectZeroPaymentHistory() {

        CreditProfile profile = createProfile(
                "50000", "10000",
                "100000", "20000",
                0, 0, 1, 1, 48
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(profile)
        );
    }

    @Test
    void shouldRejectZeroCreditLimit() {

        CreditProfile profile = createProfile(
                "50000", "10000",
                "0", "0",
                12, 12, 1, 1, 48
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(profile)
        );
    }

    @Test
    void shouldRejectNegativeIncome() {

        CreditProfile profile = createProfile(
                "-1000", "10000",
                "100000", "20000",
                12, 12, 1, 1, 48
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(profile)
        );
    }

    @Test
    void shouldRejectInvalidPaymentCounts() {

        CreditProfile profile = createProfile(
                "50000", "10000",
                "100000", "20000",
                12, 15, 1, 1, 48
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(profile)
        );
    }

    @Test
    void shouldRejectNullProfile() {

        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(null)
        );
    }

    @Test
    void shouldHandleOverLimitCreditBalance() {

        CreditProfile profile = createProfile(
                "50000", "10000",
                "100000", "120000",
                12, 12, 1, 1, 48
        );

        CreditScoreResult result = calculator.calculate(profile);

        assertEquals(
                0,
                BigDecimal.ZERO.compareTo(
                        result.creditUtilizationScore()
                )
        );
    }
}
