
package com.loanengine.application.service;

import com.loanengine.application.entity.Customer;
import com.loanengine.application.exception.DuplicateResourceException;
import com.loanengine.application.exception.ResourceNotFoundException;
import com.loanengine.application.repository.CustomerRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer(
                "John",
                "Doe",
                "test@example.com",
                "9876543210",
                LocalDate.of(1995, 5, 15)
        );
    }

    @Test
    void createCustomer_shouldSaveCustomer() {

        when(customerRepository.existsByEmail("test@example.com"))
                .thenReturn(false);

        when(customerRepository.save(customer))
                .thenReturn(customer);

        Customer result = customerService.createCustomer(customer);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());

        verify(customerRepository).existsByEmail("test@example.com");
        verify(customerRepository).save(customer);
    }

    @Test
    void createCustomer_shouldRejectDuplicateEmail() {

        when(customerRepository.existsByEmail("test@example.com"))
                .thenReturn(true);

        assertThrows(
                DuplicateResourceException.class,
                () -> customerService.createCustomer(customer)
        );

        verify(customerRepository).existsByEmail("test@example.com");

        verify(customerRepository, never())
                .save(any(Customer.class));
    }

    @Test
    void getCustomerById_shouldReturnCustomer() {

        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        Customer result = customerService.getCustomerById(1L);

        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("test@example.com", result.getEmail());

        verify(customerRepository).findById(1L);
    }

    @Test
    void getCustomerById_shouldThrowWhenMissing() {

        when(customerRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> customerService.getCustomerById(999L)
        );

        verify(customerRepository).findById(999L);
    }
}
