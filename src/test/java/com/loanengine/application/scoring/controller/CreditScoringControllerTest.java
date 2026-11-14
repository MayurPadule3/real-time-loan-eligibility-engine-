
package com.loanengine.application.scoring.controller;

import com.loanengine.application.entity.CreditProfile;
import com.loanengine.application.scoring.CreditRiskCategory;
import com.loanengine.application.scoring.CreditScoreResponse;
import com.loanengine.application.scoring.CreditScoreResult;
import com.loanengine.application.scoring.service.CreditScoringService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditScoringControllerTest {

    @Mock
    private CreditScoringService creditScoringService;

    @InjectMocks
    private CreditScoringController creditScoringController;

    private CreditProfile creditProfile;

    @BeforeEach
    void setUp() {
        creditProfile = mock(CreditProfile.class);
        when(creditProfile.getId()).thenReturn(10L);
    }

    @Test
    void shouldCalculateCreditScoreSuccessfully() {

        CreditScoreResult result = new CreditScoreResult(
                808,
                CreditRiskCategory.LOW,
                new BigDecimal("91.67"),
                new BigDecimal("80"),
                new BigDecimal("85"),
                new BigDecimal("55"),
                new BigDecimal("100"),
                "1.0"
        );

        when(creditScoringService.getLatestCreditProfile(1L))
                .thenReturn(creditProfile);

        when(creditScoringService.calculateCreditScore(creditProfile))
                .thenReturn(result);

        ResponseEntity<CreditScoreResponse> response =
                creditScoringController.calculateCreditScore(1L);

        assertEquals(200, response.getStatusCode().value());

        CreditScoreResponse body = response.getBody();

        assertNotNull(body);
        assertEquals(1L, body.customerId());
        assertEquals(10L, body.creditProfileId());
        assertEquals(808, body.creditScore());
        assertEquals(CreditRiskCategory.LOW, body.riskCategory());
        assertEquals(new BigDecimal("91.67"), body.paymentHistoryScore());
        assertEquals(new BigDecimal("80"), body.creditUtilizationScore());
        assertEquals(new BigDecimal("85"), body.debtToIncomeScore());
        assertEquals(new BigDecimal("55"), body.creditHistoryScore());
        assertEquals(new BigDecimal("100"), body.creditMixScore());
        assertEquals("1.0", body.modelVersion());

        verify(creditScoringService).getLatestCreditProfile(1L);
        verify(creditScoringService).calculateCreditScore(creditProfile);
    }

    @Test
    void shouldReturnMediumRiskScore() {

        CreditScoreResult result = new CreditScoreResult(
                700,
                CreditRiskCategory.MEDIUM,
                new BigDecimal("80"),
                new BigDecimal("70"),
                new BigDecimal("75"),
                new BigDecimal("60"),
                new BigDecimal("80"),
                "1.0"
        );

        when(creditScoringService.getLatestCreditProfile(2L))
                .thenReturn(creditProfile);

        when(creditScoringService.calculateCreditScore(creditProfile))
                .thenReturn(result);

        ResponseEntity<CreditScoreResponse> response =
                creditScoringController.calculateCreditScore(2L);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(700, response.getBody().creditScore());
        assertEquals(CreditRiskCategory.MEDIUM,
                response.getBody().riskCategory());
    }

    @Test
    void shouldReturnHighRiskScore() {

        CreditScoreResult result = new CreditScoreResult(
                600,
                CreditRiskCategory.HIGH,
                new BigDecimal("50"),
                new BigDecimal("30"),
                new BigDecimal("40"),
                new BigDecimal("35"),
                new BigDecimal("50"),
                "1.0"
        );

        when(creditScoringService.getLatestCreditProfile(3L))
                .thenReturn(creditProfile);

        when(creditScoringService.calculateCreditScore(creditProfile))
                .thenReturn(result);

        ResponseEntity<CreditScoreResponse> response =
                creditScoringController.calculateCreditScore(3L);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(600, response.getBody().creditScore());
        assertEquals(CreditRiskCategory.HIGH,
                response.getBody().riskCategory());
    }
}
