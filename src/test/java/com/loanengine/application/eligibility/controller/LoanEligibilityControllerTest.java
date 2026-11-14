
package com.loanengine.application.eligibility.controller;

import com.loanengine.application.eligibility.model.EligibilityDecision;
import com.loanengine.application.eligibility.model.EligibilityReason;
import com.loanengine.application.eligibility.model.EligibilityResult;
import com.loanengine.application.exception.ResourceNotFoundException;
import com.loanengine.application.scoring.CreditRiskCategory;
import com.loanengine.application.service.LoanEligibilityService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanEligibilityControllerTest {

    @Mock
    private LoanEligibilityService loanEligibilityService;

    @InjectMocks
    private LoanEligibilityController loanEligibilityController;

    private EligibilityResult eligibilityResult;

    @BeforeEach
    void setUp() {

        eligibilityResult = new EligibilityResult(
                EligibilityDecision.APPROVED,
                List.of(EligibilityReason.ELIGIBLE),
                new BigDecimal("25.00"),
                750,
                CreditRiskCategory.LOW,
                "1.0"
        );
    }

    @Test
    void evaluateLoanEligibilityShouldReturnApprovedResult() {

        Long loanApplicationId = 100L;

        when(loanEligibilityService.evaluateLoanEligibility(loanApplicationId))
                .thenReturn(eligibilityResult);

        ResponseEntity<EligibilityResult> response =
                loanEligibilityController.evaluateLoanEligibility(
                        loanApplicationId
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        assertNotNull(response.getBody());

        assertEquals(
                EligibilityDecision.APPROVED,
                response.getBody().decision()
        );

        assertEquals(750, response.getBody().creditScore());

        assertEquals(
                CreditRiskCategory.LOW,
                response.getBody().creditRiskCategory()
        );

        verify(loanEligibilityService)
                .evaluateLoanEligibility(loanApplicationId);
    }

    @Test
    void evaluateLoanEligibilityShouldReturnRejectedResult() {

        Long loanApplicationId = 101L;

        EligibilityResult rejectedResult = new EligibilityResult(
                EligibilityDecision.REJECTED,
                List.of(EligibilityReason.INSUFFICIENT_INCOME),
                new BigDecimal("55.00"),
                580,
                CreditRiskCategory.HIGH,
                "1.0"
        );

        when(loanEligibilityService.evaluateLoanEligibility(loanApplicationId))
                .thenReturn(rejectedResult);

        ResponseEntity<EligibilityResult> response =
                loanEligibilityController.evaluateLoanEligibility(
                        loanApplicationId
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        assertNotNull(response.getBody());

        assertEquals(
                EligibilityDecision.REJECTED,
                response.getBody().decision()
        );

        assertEquals(
                EligibilityReason.INSUFFICIENT_INCOME,
                response.getBody().reasons().get(0)
        );

        verify(loanEligibilityService)
                .evaluateLoanEligibility(loanApplicationId);
    }

    @Test
    void evaluateLoanEligibilityShouldReturnManualReviewResult() {

        Long loanApplicationId = 102L;

        EligibilityResult manualReviewResult = new EligibilityResult(
                EligibilityDecision.MANUAL_REVIEW,
                List.of(EligibilityReason.ADDITIONAL_REVIEW_REQUIRED),
                new BigDecimal("45.00"),
                630,
                CreditRiskCategory.MEDIUM,
                "1.0"
        );

        when(loanEligibilityService.evaluateLoanEligibility(loanApplicationId))
                .thenReturn(manualReviewResult);

        ResponseEntity<EligibilityResult> response =
                loanEligibilityController.evaluateLoanEligibility(
                        loanApplicationId
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        assertNotNull(response.getBody());

        assertEquals(
                EligibilityDecision.MANUAL_REVIEW,
                response.getBody().decision()
        );

        verify(loanEligibilityService)
                .evaluateLoanEligibility(loanApplicationId);
    }

    @Test
    void evaluateLoanEligibilityShouldPropagateResourceNotFoundException() {

        Long loanApplicationId = 999L;

        when(loanEligibilityService.evaluateLoanEligibility(loanApplicationId))
                .thenThrow(new ResourceNotFoundException(
                        "Loan application not found with ID: 999"
                ));

        assertThrows(
                ResourceNotFoundException.class,
                () -> loanEligibilityController.evaluateLoanEligibility(
                        loanApplicationId
                )
        );

        verify(loanEligibilityService)
                .evaluateLoanEligibility(loanApplicationId);
    }

    @Test
    void evaluateLoanEligibilityShouldPropagateIllegalArgumentException() {

        Long loanApplicationId = 0L;

        when(loanEligibilityService.evaluateLoanEligibility(loanApplicationId))
                .thenThrow(new IllegalArgumentException(
                        "A valid loan application ID is required."
                ));

        assertThrows(
                IllegalArgumentException.class,
                () -> loanEligibilityController.evaluateLoanEligibility(
                        loanApplicationId
                )
        );

        verify(loanEligibilityService)
                .evaluateLoanEligibility(loanApplicationId);
    }
}
