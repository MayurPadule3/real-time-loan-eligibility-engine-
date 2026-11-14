
package com.loanengine.application.service;

import com.loanengine.application.entity.CreditDataSource;
import com.loanengine.application.entity.CreditProfile;
import com.loanengine.application.entity.Customer;
import com.loanengine.application.entity.EmploymentType;
import com.loanengine.application.exception.ResourceNotFoundException;
import com.loanengine.application.repository.CreditProfileRepository;
import com.loanengine.application.repository.CustomerRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditProfileServiceTest {

    @Mock
    private CreditProfileRepository creditProfileRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CreditProfileService creditProfileService;

    // Test data helper
    private CreditProfile createTestCreditProfile() {

        Customer customer = mock(Customer.class);

        return new CreditProfile(
                customer,
                new BigDecimal("50000.00"),
                new BigDecimal("10000.00"),
                EmploymentType.SALARIED,
                new BigDecimal("100000.00"),
                new BigDecimal("30000.00"),
                12,
                11,
                1,
                2,
                36,
                CreditDataSource.SYNTHETIC_DEMO,
                LocalDate.now()
        );
    }

    // Test 1: Successfully create credit profile
    @Test
    void createCreditProfile_ShouldReturnSavedProfile() {

        CreditProfile profile = createTestCreditProfile();

        when(profile.getCustomer().getId()).thenReturn(1L);
        when(customerRepository.existsById(1L)).thenReturn(true);
        when(creditProfileRepository.save(profile)).thenReturn(profile);

        CreditProfile result =
                creditProfileService.createCreditProfile(profile);

        assertNotNull(result);
        assertSame(profile, result);

        verify(customerRepository).existsById(1L);
        verify(creditProfileRepository).save(profile);
    }

    // Test 2: Reject creation when customer does not exist
    @Test
    void createCreditProfile_ShouldThrowException_WhenCustomerDoesNotExist() {

        CreditProfile profile = createTestCreditProfile();

        when(profile.getCustomer().getId()).thenReturn(1L);
        when(customerRepository.existsById(1L)).thenReturn(false);

        assertThrows(
                ResourceNotFoundException.class,
                () -> creditProfileService.createCreditProfile(profile)
        );

        verify(creditProfileRepository, never()).save(any());
    }

    // Test 3: Reject invalid payment history
    @Test
    void createCreditProfile_ShouldThrowException_WhenOnTimePaymentsExceedTotal() {

        CreditProfile profile = new CreditProfile(
                mock(Customer.class),
                new BigDecimal("50000.00"),
                new BigDecimal("10000.00"),
                EmploymentType.SALARIED,
                new BigDecimal("100000.00"),
                new BigDecimal("30000.00"),
                10,
                12,
                1,
                2,
                36,
                CreditDataSource.SYNTHETIC_DEMO,
                LocalDate.now()
        );

        when(profile.getCustomer().getId()).thenReturn(1L);
        when(customerRepository.existsById(1L)).thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> creditProfileService.createCreditProfile(profile)
        );

        verify(creditProfileRepository, never()).save(any());
    }

    // Test 4: Successfully retrieve credit profile by ID
    @Test
    void getCreditProfileById_ShouldReturnProfile() {

        CreditProfile profile = createTestCreditProfile();

        when(creditProfileRepository.findById(1L))
                .thenReturn(Optional.of(profile));

        CreditProfile result =
                creditProfileService.getCreditProfileById(1L);

        assertNotNull(result);
        assertSame(profile, result);

        verify(creditProfileRepository).findById(1L);
    }

    // Test 5: Throw exception when credit profile is not found
    @Test
    void getCreditProfileById_ShouldThrowException_WhenNotFound() {

        when(creditProfileRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> creditProfileService.getCreditProfileById(99L)
        );

        verify(creditProfileRepository).findById(99L);
    }

    // Test 6: Retrieve all credit profiles for a customer
    @Test
    void getCreditProfilesByCustomerId_ShouldReturnProfiles() {

        CreditProfile profile = createTestCreditProfile();

        when(customerRepository.existsById(1L)).thenReturn(true);
        when(creditProfileRepository.findByCustomerId(1L))
                .thenReturn(List.of(profile));

        List<CreditProfile> result =
                creditProfileService.getCreditProfilesByCustomerId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertSame(profile, result.get(0));

        verify(customerRepository).existsById(1L);
        verify(creditProfileRepository).findByCustomerId(1L);
    }

    // Test 7: Retrieve latest credit profile for a customer
    @Test
    void getLatestCreditProfile_ShouldReturnLatestProfile() {

        CreditProfile profile = createTestCreditProfile();

        when(customerRepository.existsById(1L)).thenReturn(true);
        when(creditProfileRepository
                .findTopByCustomerIdOrderByAsOfDateDescCreatedAtDesc(1L))
                .thenReturn(Optional.of(profile));

        CreditProfile result =
                creditProfileService.getLatestCreditProfile(1L);

        assertNotNull(result);
        assertSame(profile, result);

        verify(customerRepository).existsById(1L);
        verify(creditProfileRepository)
                .findTopByCustomerIdOrderByAsOfDateDescCreatedAtDesc(1L);
    }

    // Test 8: Throw exception when customer has no credit profile
    @Test
    void getLatestCreditProfile_ShouldThrowException_WhenNoProfileExists() {

        when(customerRepository.existsById(1L)).thenReturn(true);
        when(creditProfileRepository
                .findTopByCustomerIdOrderByAsOfDateDescCreatedAtDesc(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> creditProfileService.getLatestCreditProfile(1L)
        );

        verify(customerRepository).existsById(1L);
        verify(creditProfileRepository)
                .findTopByCustomerIdOrderByAsOfDateDescCreatedAtDesc(1L);
    }
}
