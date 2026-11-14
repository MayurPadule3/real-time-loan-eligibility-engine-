
package com.loanengine.application.scoring.service;

import com.loanengine.application.entity.CreditProfile;
import com.loanengine.application.entity.CreditDataSource;
import com.loanengine.application.entity.EmploymentType;
import com.loanengine.application.scoring.CreditScoreCalculator;
import com.loanengine.application.scoring.CreditScoreResult;
import com.loanengine.application.scoring.CreditRiskCategory;
import com.loanengine.application.service.CreditProfileService;

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
class CreditScoringServiceTest {

    @Mock
    private CreditScoreCalculator creditScoreCalculator;

    @Mock
    private CreditProfileService creditProfileService;

    @InjectMocks
    private CreditScoringService creditScoringService;

    private CreditProfile creditProfile;

    private CreditScoreResult creditScoreResult;

    @BeforeEach
    void setUp() {

        creditProfile = new CreditProfile(
                null,
                new BigDecimal("65000"),
                new BigDecimal("15000"),
                EmploymentType.SALARIED,
                new BigDecimal("100000"),
                new BigDecimal("25000"),
                12,
                11,
                1,
                2,
                48,
                CreditDataSource.SYNTHETIC_DEMO,
                LocalDate.of(2026, 11, 14)
        );

        creditScoreResult = new CreditScoreResult(
                808,
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
    void shouldRetrieveLatestCreditProfile() {

        Long customerId = 1L;

        when(creditProfileService.getLatestCreditProfile(customerId))
                .thenReturn(creditProfile);

        CreditProfile result =
                creditScoringService.getLatestCreditProfile(customerId);

        assertNotNull(result);
        assertSame(creditProfile, result);

        verify(creditProfileService)
                .getLatestCreditProfile(customerId);
    }

    @Test
    void shouldCalculateCreditScoreSuccessfully() {

        when(creditScoreCalculator.calculate(creditProfile))
                .thenReturn(creditScoreResult);

        CreditScoreResult result =
                creditScoringService.calculateCreditScore(creditProfile);

        assertNotNull(result);
        assertEquals(808, result.creditScore());
        assertEquals(CreditRiskCategory.LOW, result.riskCategory());
        assertEquals("1.0", result.modelVersion());

        verify(creditScoreCalculator).calculate(creditProfile);
    }

    @Test
    void shouldReturnMediumRiskScore() {

        CreditScoreResult mediumRiskResult = new CreditScoreResult(
                700,
                CreditRiskCategory.MEDIUM,
                new BigDecimal("83.33"),
                new BigDecimal("60"),
                new BigDecimal("70"),
                new BigDecimal("55"),
                new BigDecimal("70"),
                "1.0"
        );

        when(creditScoreCalculator.calculate(creditProfile))
                .thenReturn(mediumRiskResult);

        CreditScoreResult result =
                creditScoringService.calculateCreditScore(creditProfile);

        assertEquals(700, result.creditScore());
        assertEquals(CreditRiskCategory.MEDIUM, result.riskCategory());
    }

    @Test
    void shouldReturnHighRiskScore() {

        CreditScoreResult highRiskResult = new CreditScoreResult(
                600,
                CreditRiskCategory.HIGH,
                new BigDecimal("40"),
                new BigDecimal("20"),
                new BigDecimal("25"),
                new BigDecimal("15"),
                new BigDecimal("50"),
                "1.0"
        );

        when(creditScoreCalculator.calculate(creditProfile))
                .thenReturn(highRiskResult);

        CreditScoreResult result =
                creditScoringService.calculateCreditScore(creditProfile);

        assertEquals(600, result.creditScore());
        assertEquals(CreditRiskCategory.HIGH, result.riskCategory());
    }

    @Test
    void shouldPropagateInvalidProfileException() {

        when(creditScoreCalculator.calculate(null))
                .thenThrow(new IllegalArgumentException(
                        "Credit profile is required."
                ));

        assertThrows(
                IllegalArgumentException.class,
                () -> creditScoringService.calculateCreditScore(null)
        );

        verify(creditScoreCalculator).calculate(null);
    }
}
