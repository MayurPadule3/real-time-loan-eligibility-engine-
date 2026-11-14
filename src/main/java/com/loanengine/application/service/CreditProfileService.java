
package com.loanengine.application.service;

import com.loanengine.application.dto.CreditProfileRequest;
import com.loanengine.application.entity.CreditDataSource;
import com.loanengine.application.entity.CreditProfile;
import com.loanengine.application.entity.Customer;
import com.loanengine.application.exception.ResourceNotFoundException;
import com.loanengine.application.repository.CreditProfileRepository;
import com.loanengine.application.repository.CustomerRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CreditProfileService {

    private final CreditProfileRepository creditProfileRepository;
    private final CustomerRepository customerRepository;

    public CreditProfileService(
            CreditProfileRepository creditProfileRepository,
            CustomerRepository customerRepository) {

        this.creditProfileRepository = creditProfileRepository;
        this.customerRepository = customerRepository;
    }

    // Create a credit profile snapshot using an entity
    // Retained for backward compatibility with existing tests.
    public CreditProfile createCreditProfile(CreditProfile creditProfile) {

        if (creditProfile == null || creditProfile.getCustomer() == null) {
            throw new IllegalArgumentException(
                    "Credit profile and customer are required."
            );
        }

        Long customerId = creditProfile.getCustomer().getId();

        if (customerId == null || !customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException(
                    "Customer not found with ID: " + customerId
            );
        }

        validateCreditProfile(creditProfile);

        return creditProfileRepository.save(creditProfile);
    }

    // Create a credit profile snapshot using an API request DTO
    public CreditProfile createCreditProfile(CreditProfileRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Credit profile request is required."
            );
        }

        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with ID: " + request.customerId()
                ));

        CreditProfile creditProfile = new CreditProfile(
                customer,
                request.monthlyIncome(),
                request.monthlyDebtObligations(),
                request.employmentType(),
                request.revolvingCreditLimit(),
                request.revolvingCreditBalance(),
                request.totalPaymentsLast12Months(),
                request.onTimePaymentsLast12Months(),
                request.securedCreditAccounts(),
                request.unsecuredCreditAccounts(),
                request.creditHistoryMonths(),
                CreditDataSource.SYNTHETIC_DEMO,
                request.asOfDate()
        );

        validateCreditProfile(creditProfile);

        return creditProfileRepository.save(creditProfile);
    }

    // Retrieve a credit profile by ID
    @Transactional(readOnly = true)
    public CreditProfile getCreditProfileById(Long id) {

        return creditProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Credit profile not found with ID: " + id
                ));
    }

    // Retrieve all credit profile snapshots for a customer
    @Transactional(readOnly = true)
    public List<CreditProfile> getCreditProfilesByCustomerId(
            Long customerId) {

        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException(
                    "Customer not found with ID: " + customerId
            );
        }

        return creditProfileRepository.findByCustomerId(customerId);
    }

    // Retrieve the latest credit profile snapshot for a customer
    @Transactional(readOnly = true)
    public CreditProfile getLatestCreditProfile(Long customerId) {

        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException(
                    "Customer not found with ID: " + customerId
            );
        }

        return creditProfileRepository
                .findTopByCustomerIdOrderByAsOfDateDescCreatedAtDesc(
                        customerId
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No credit profile found for customer ID: "
                                + customerId
                ));
    }

    // Validate consistency between related credit profile fields
    private void validateCreditProfile(CreditProfile creditProfile) {

        if (creditProfile.getCustomer() == null) {
            throw new IllegalArgumentException(
                    "Customer is required."
            );
        }

        if (creditProfile.getOnTimePaymentsLast12Months() == null
                || creditProfile.getTotalPaymentsLast12Months() == null) {

            throw new IllegalArgumentException(
                    "Payment history values are required."
            );
        }

        if (creditProfile.getOnTimePaymentsLast12Months()
                > creditProfile.getTotalPaymentsLast12Months()) {

            throw new IllegalArgumentException(
                    "On-time payments cannot exceed total payments."
            );
        }
    }
}
