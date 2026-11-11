
package com.loanengine.application.service;

import com.loanengine.application.dto.LoanApplicationRequest;
import com.loanengine.application.dto.LoanApplicationResponse;
import com.loanengine.application.entity.Customer;
import com.loanengine.application.entity.LoanApplication;
import com.loanengine.application.entity.LoanStatus;
import com.loanengine.application.exception.ResourceNotFoundException;
import com.loanengine.application.repository.CustomerRepository;
import com.loanengine.application.repository.LoanApplicationRepository;

import org.junit.jupiter.api.BeforeEach;
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
class LoanApplicationServiceTest {

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private LoanApplicationService loanApplicationService;

    private Customer customer;
    private LoanApplicationRequest request;

    @BeforeEach
    void setUp() {

        customer = new Customer(
                "John",
                "Doe",
                "john@example.com",
                "9876543210",
                LocalDate.of(1995, 5, 15)
        );

        request = new LoanApplicationRequest();
        request.setCustomerId(1L);
        request.setLoanAmount(new BigDecimal("500000.00"));
        request.setInterestRate(new BigDecimal("10.50"));
        request.setTenureMonths(60);
        request.setLoanPurpose("Home Renovation");
    }

    @Test
    void createLoanApplication_shouldCreateSuccessfully() {

        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        when(loanApplicationRepository.save(any(LoanApplication.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LoanApplicationResponse response =
                loanApplicationService.createLoanApplication(request);

        assertNotNull(response);
        assertEquals(
                new BigDecimal("500000.00"),
                response.getLoanAmount()
        );
        assertEquals(
                new BigDecimal("10.50"),
                response.getInterestRate()
        );
        assertEquals(60, response.getTenureMonths());
        assertEquals("Home Renovation", response.getLoanPurpose());
        assertEquals(LoanStatus.PENDING, response.getStatus());

        verify(customerRepository).findById(1L);
        verify(loanApplicationRepository).save(any(LoanApplication.class));
    }

    @Test
    void createLoanApplication_shouldThrowWhenCustomerDoesNotExist() {

        when(customerRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> loanApplicationService.createLoanApplication(request)
        );

        verify(customerRepository).findById(1L);
        verify(loanApplicationRepository, never())
                .save(any(LoanApplication.class));
    }

    @Test
    void getLoanApplicationById_shouldReturnApplication() {

        LoanApplication application = new LoanApplication(
                customer,
                new BigDecimal("500000.00"),
                new BigDecimal("10.50"),
                60,
                "Home Renovation"
        );

        when(loanApplicationRepository.findById(1L))
                .thenReturn(Optional.of(application));

        LoanApplicationResponse response =
                loanApplicationService.getLoanApplicationById(1L);

        assertNotNull(response);
        assertEquals(
                new BigDecimal("500000.00"),
                response.getLoanAmount()
        );
        assertEquals(LoanStatus.PENDING, response.getStatus());

        verify(loanApplicationRepository).findById(1L);
    }

    @Test
    void getLoanApplicationById_shouldThrowWhenApplicationDoesNotExist() {

        when(loanApplicationRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> loanApplicationService.getLoanApplicationById(999L)
        );

        verify(loanApplicationRepository).findById(999L);
    }

    @Test
    void getLoanApplicationsByCustomerId_shouldReturnApplications() {

        LoanApplication application = new LoanApplication(
                customer,
                new BigDecimal("500000.00"),
                new BigDecimal("10.50"),
                60,
                "Home Renovation"
        );

        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        when(loanApplicationRepository.findByCustomerId(1L))
                .thenReturn(List.of(application));

        List<LoanApplicationResponse> responses =
                loanApplicationService.getLoanApplicationsByCustomerId(1L);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(
                new BigDecimal("500000.00"),
                responses.get(0).getLoanAmount()
        );

        verify(customerRepository).findById(1L);
        verify(loanApplicationRepository).findByCustomerId(1L);
    }
}
