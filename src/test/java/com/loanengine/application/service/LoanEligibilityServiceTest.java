
package com.loanengine.application.service;

import com.loanengine.application.eligibility.model.EligibilityDecision;
import com.loanengine.application.eligibility.model.EligibilityReason;
import com.loanengine.application.eligibility.model.EligibilityResult;
import com.loanengine.application.eligibility.rules.EligibilityRuleEvaluator;
import com.loanengine.application.entity.CreditProfile;
import com.loanengine.application.entity.Customer;
import com.loanengine.application.entity.LoanApplication;
import com.loanengine.application.exception.ResourceNotFoundException;
import com.loanengine.application.repository.LoanApplicationRepository;
import com.loanengine.application.scoring.CreditRiskCategory;
import com.loanengine.application.scoring.CreditScoreResult;
import com.loanengine.application.scoring.service.CreditScoringService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanEligibilityServiceTest {

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private CreditProfileService creditProfileService;

    @Mock
    private CreditScoringService creditScoringService;

    @Mock
    private EligibilityRuleEvaluator eligibilityRuleEvaluator;

    @InjectMocks
    private LoanEligibilityService loanEligibilityService;

    private LoanApplication loanApplication;
    private CreditProfile creditProfile;
    private Customer customer;
    private CreditScoreResult creditScoreResult;
    private EligibilityResult eligibilityResult;

    @BeforeEach
    void setUp() {

        customer = mock(Customer.class);
        loanApplication = mock(LoanApplication.class);
        creditProfile = mock(CreditProfile.class);

        creditScoreResult = new CreditScoreResult(
                750,
                CreditRiskCategory.LOW,
                new BigDecimal("95.00"),
                new BigDecimal("90.00"),
                new BigDecimal("85.00"),
                new BigDecimal("80.00"),
                new BigDecimal("100.00"),
                "1.0"
        );

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
    void evaluateLoanEligibilityShouldReturnEligibilityResult() {

        Long loanApplicationId = 100L;

        when(customer.getId()).thenReturn(1L);
        when(loanApplication.getCustomer()).thenReturn(customer);

        when(loanApplicationRepository.findById(loanApplicationId))
                .thenReturn(Optional.of(loanApplication));

        when(creditProfileService.getLatestCreditProfile(1L))
                .thenReturn(creditProfile);

        when(creditProfile.getTotalPaymentsLast12Months())
                .thenReturn(12);

        when(creditProfile.getOnTimePaymentsLast12Months())
                .thenReturn(11);

        when(creditScoringService.calculateCreditScore(creditProfile))
                .thenReturn(creditScoreResult);

        when(eligibilityRuleEvaluator.evaluate(
                loanApplication,
                creditProfile,
                creditScoreResult
        )).thenReturn(eligibilityResult);

        EligibilityResult actualResult =
                loanEligibilityService.evaluateLoanEligibility(
                        loanApplicationId
                );

        assertNotNull(actualResult);
        assertEquals(EligibilityDecision.APPROVED, actualResult.decision());
        assertEquals(750, actualResult.creditScore());

        verify(loanApplicationRepository).findById(loanApplicationId);
        verify(creditProfileService).getLatestCreditProfile(1L);
        verify(creditScoringService).calculateCreditScore(creditProfile);

        verify(eligibilityRuleEvaluator).evaluate(
                loanApplication,
                creditProfile,
                creditScoreResult
        );
    }

    @Test
    void evaluateLoanEligibilityShouldSkipScoringWhenPaymentHistoryIsMissing() {

        Long loanApplicationId = 100L;

        when(customer.getId()).thenReturn(1L);
        when(loanApplication.getCustomer()).thenReturn(customer);

        when(loanApplicationRepository.findById(loanApplicationId))
                .thenReturn(Optional.of(loanApplication));

        when(creditProfileService.getLatestCreditProfile(1L))
                .thenReturn(creditProfile);

        when(creditProfile.getTotalPaymentsLast12Months())
                .thenReturn(0);

        when(creditProfile.getOnTimePaymentsLast12Months())
                .thenReturn(0);

        when(eligibilityRuleEvaluator.evaluate(
                loanApplication,
                creditProfile,
                null
        )).thenReturn(eligibilityResult);

        EligibilityResult actualResult =
                loanEligibilityService.evaluateLoanEligibility(
                        loanApplicationId
                );

        assertNotNull(actualResult);

        verify(creditScoringService, never())
                .calculateCreditScore(any());

        verify(eligibilityRuleEvaluator).evaluate(
                loanApplication,
                creditProfile,
                null
        );
    }

    @Test
    void evaluateLoanEligibilityShouldThrowExceptionWhenLoanApplicationDoesNotExist() {

        Long loanApplicationId = 999L;

        when(loanApplicationRepository.findById(loanApplicationId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> loanEligibilityService.evaluateLoanEligibility(
                        loanApplicationId
                )
        );

        verify(creditProfileService, never())
                .getLatestCreditProfile(any());

        verify(eligibilityRuleEvaluator, never())
                .evaluate(any(), any(), any());
    }

    @Test
    void evaluateLoanEligibilityShouldThrowExceptionWhenCreditProfileDoesNotExist() {

        Long loanApplicationId = 100L;

        when(customer.getId()).thenReturn(1L);
        when(loanApplication.getCustomer()).thenReturn(customer);

        when(loanApplicationRepository.findById(loanApplicationId))
                .thenReturn(Optional.of(loanApplication));

        when(creditProfileService.getLatestCreditProfile(1L))
                .thenThrow(new ResourceNotFoundException(
                        "Credit profile not found."
                ));

        assertThrows(
                ResourceNotFoundException.class,
                () -> loanEligibilityService.evaluateLoanEligibility(
                        loanApplicationId
                )
        );

        verify(creditScoringService, never())
                .calculateCreditScore(any());
    }

    @Test
    void evaluateLoanEligibilityShouldRejectNullLoanApplicationId() {

        assertThrows(
                IllegalArgumentException.class,
                () -> loanEligibilityService.evaluateLoanEligibility(null)
        );

        verifyNoInteractions(
                loanApplicationRepository,
                creditProfileService,
                creditScoringService,
                eligibilityRuleEvaluator
        );
    }

    @Test
    void evaluateLoanEligibilityShouldRejectInvalidLoanApplicationId() {

        assertThrows(
                IllegalArgumentException.class,
                () -> loanEligibilityService.evaluateLoanEligibility(0L)
        );

        verifyNoInteractions(
                loanApplicationRepository,
                creditProfileService,
                creditScoringService,
                eligibilityRuleEvaluator
        );
    }
}
