
package com.loanengine.application.service;

import com.loanengine.application.entity.CreditProfile;
import com.loanengine.application.entity.Customer;
import com.loanengine.application.entity.LoanApplication;
import com.loanengine.application.entity.LoanStatus;

import com.loanengine.application.exception.ResourceNotFoundException;

import com.loanengine.application.repository.LoanApplicationRepository;

import com.loanengine.application.scoring.CreditScoreResult;
import com.loanengine.application.scoring.CreditRiskCategory;
import com.loanengine.application.scoring.service.CreditScoringService;

import com.loanengine.application.eligibility.model.EligibilityDecision;
import com.loanengine.application.eligibility.model.EligibilityReason;
import com.loanengine.application.eligibility.model.EligibilityResult;
import com.loanengine.application.eligibility.rules.EligibilityRuleEvaluator;

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
    private Customer customer;
    private CreditProfile creditProfile;
    private CreditScoreResult creditScoreResult;
    private EligibilityResult eligibilityResult;

    private final Long loanApplicationId = 1L;
    private final Long customerId = 10L;

    @BeforeEach
    void setUp() {

        // Create mocks only.
        // Do not put common Mockito stubbings here.
        customer = mock(Customer.class);
        loanApplication = mock(LoanApplication.class);
        creditProfile = mock(CreditProfile.class);

        creditScoreResult = new CreditScoreResult(
                808,
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
                new BigDecimal("39.61"),
                808,
                CreditRiskCategory.LOW,
                "1.0"
        );
    }

    /**
     * Configures a valid loan application for eligibility evaluation.
     */
    private void configureValidLoanApplication() {

        when(loanApplication.getStatus())
                .thenReturn(LoanStatus.PENDING);

        when(loanApplication.getCustomer())
                .thenReturn(customer);

        when(customer.getId())
                .thenReturn(customerId);
    }

    @Test
    void evaluateLoanEligibilityShouldReturnEligibilityResult() {

        // Arrange
        configureValidLoanApplication();

        when(loanApplicationRepository.findById(loanApplicationId))
                .thenReturn(Optional.of(loanApplication));

        when(creditProfileService.getLatestCreditProfile(customerId))
                .thenReturn(creditProfile);

        when(creditProfile.getTotalPaymentsLast12Months())
                .thenReturn(12);

        when(creditProfile.getOnTimePaymentsLast12Months())
                .thenReturn(10);

        when(creditScoringService.calculateCreditScore(creditProfile))
                .thenReturn(creditScoreResult);

        when(eligibilityRuleEvaluator.evaluate(
                loanApplication,
                creditProfile,
                creditScoreResult
        )).thenReturn(eligibilityResult);

        // Act
        EligibilityResult result =
                loanEligibilityService.evaluateLoanEligibility(
                        loanApplicationId
                );

        // Assert
        assertNotNull(result);

        assertEquals(
                EligibilityDecision.APPROVED,
                result.decision()
        );

        assertEquals(808, result.creditScore());

        assertEquals(
                CreditRiskCategory.LOW,
                result.creditRiskCategory()
        );

        verify(loanApplicationRepository)
                .findById(loanApplicationId);

        verify(creditProfileService)
                .getLatestCreditProfile(customerId);

        verify(creditScoringService)
                .calculateCreditScore(creditProfile);

        verify(eligibilityRuleEvaluator)
                .evaluate(
                        loanApplication,
                        creditProfile,
                        creditScoreResult
                );
    }

    @Test
    void evaluateLoanEligibilityShouldThrowExceptionWhenIdIsInvalid() {

        // Act and Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> loanEligibilityService.evaluateLoanEligibility(null)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> loanEligibilityService.evaluateLoanEligibility(0L)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> loanEligibilityService.evaluateLoanEligibility(-1L)
        );

        verifyNoInteractions(
                loanApplicationRepository,
                creditProfileService,
                creditScoringService,
                eligibilityRuleEvaluator
        );
    }

    @Test
    void evaluateLoanEligibilityShouldThrowExceptionWhenLoanApplicationDoesNotExist() {

        // Arrange
        when(loanApplicationRepository.findById(loanApplicationId))
                .thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(
                ResourceNotFoundException.class,
                () -> loanEligibilityService.evaluateLoanEligibility(
                        loanApplicationId
                )
        );

        verify(loanApplicationRepository)
                .findById(loanApplicationId);

        verifyNoInteractions(
                creditProfileService,
                creditScoringService,
                eligibilityRuleEvaluator
        );
    }

    @Test
    void evaluateLoanEligibilityShouldThrowExceptionWhenCreditProfileDoesNotExist() {

        // Arrange
        configureValidLoanApplication();

        when(loanApplicationRepository.findById(loanApplicationId))
                .thenReturn(Optional.of(loanApplication));

        when(creditProfileService.getLatestCreditProfile(customerId))
                .thenThrow(new ResourceNotFoundException(
                        "No credit profile found for customer ID: "
                                + customerId
                ));

        // Act and Assert
        assertThrows(
                ResourceNotFoundException.class,
                () -> loanEligibilityService.evaluateLoanEligibility(
                        loanApplicationId
                )
        );

        verify(creditProfileService)
                .getLatestCreditProfile(customerId);

        verifyNoInteractions(
                creditScoringService,
                eligibilityRuleEvaluator
        );
    }

    @Test
    void evaluateLoanEligibilityShouldSkipScoringWhenPaymentHistoryIsMissing() {

        // Arrange
        configureValidLoanApplication();

        when(loanApplicationRepository.findById(loanApplicationId))
                .thenReturn(Optional.of(loanApplication));

        when(creditProfileService.getLatestCreditProfile(customerId))
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

        // Act
        EligibilityResult result =
                loanEligibilityService.evaluateLoanEligibility(
                        loanApplicationId
                );

        // Assert
        assertNotNull(result);

        verify(creditScoringService, never())
                .calculateCreditScore(any(CreditProfile.class));

        verify(eligibilityRuleEvaluator)
                .evaluate(
                        loanApplication,
                        creditProfile,
                        null
                );
    }

    @Test
    void evaluateLoanEligibilityShouldThrowExceptionWhenApplicationStatusIsInvalid() {

        // Arrange
        when(loanApplicationRepository.findById(loanApplicationId))
                .thenReturn(Optional.of(loanApplication));

        when(loanApplication.getStatus())
                .thenReturn(LoanStatus.APPROVED);

        // Act and Assert
        assertThrows(
                IllegalStateException.class,
                () -> loanEligibilityService.evaluateLoanEligibility(
                        loanApplicationId
                )
        );

        verifyNoInteractions(
                creditProfileService,
                creditScoringService,
                eligibilityRuleEvaluator
        );
    }
}
