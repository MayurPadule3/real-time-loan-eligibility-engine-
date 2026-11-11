
package com.loanengine.application.service;

import com.loanengine.application.dto.LoanApplicationRequest;
import com.loanengine.application.dto.LoanApplicationResponse;
import com.loanengine.application.entity.Customer;
import com.loanengine.application.entity.LoanApplication;
import com.loanengine.application.entity.LoanStatus;
import com.loanengine.application.exception.ResourceNotFoundException;
import com.loanengine.application.repository.CustomerRepository;
import com.loanengine.application.repository.LoanApplicationRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class LoanApplicationService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final CustomerRepository customerRepository;

    public LoanApplicationService(
            LoanApplicationRepository loanApplicationRepository,
            CustomerRepository customerRepository
    ) {
        this.loanApplicationRepository = loanApplicationRepository;
        this.customerRepository = customerRepository;
    }

    // Create a new loan application
    public LoanApplicationResponse createLoanApplication(
            LoanApplicationRequest request
    ) {

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with ID: " + request.getCustomerId()
                ));

        LoanApplication loanApplication = new LoanApplication(
                customer,
                request.getLoanAmount(),
                request.getInterestRate(),
                request.getTenureMonths(),
                request.getLoanPurpose()
        );

        LoanApplication savedApplication =
                loanApplicationRepository.save(loanApplication);

        return mapToResponse(savedApplication);
    }

    // Get a loan application by ID
    @Transactional(readOnly = true)
    public LoanApplicationResponse getLoanApplicationById(Long id) {

        LoanApplication loanApplication =
                loanApplicationRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Loan application not found with ID: " + id
                        ));

        return mapToResponse(loanApplication);
    }

    // Get all loan applications
    @Transactional(readOnly = true)
    public List<LoanApplicationResponse> getAllLoanApplications() {

        return loanApplicationRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Get loan applications belonging to a customer
    @Transactional(readOnly = true)
    public List<LoanApplicationResponse> getLoanApplicationsByCustomerId(
            Long customerId
    ) {

        customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with ID: " + customerId
                ));

        return loanApplicationRepository.findByCustomerId(customerId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Get loan applications by status
    @Transactional(readOnly = true)
    public List<LoanApplicationResponse> getLoanApplicationsByStatus(
            LoanStatus status
    ) {

        return loanApplicationRepository.findByStatus(status)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Convert entity into response DTO
    private LoanApplicationResponse mapToResponse(
            LoanApplication loanApplication
    ) {

        return new LoanApplicationResponse(
                loanApplication.getId(),
                loanApplication.getCustomer().getId(),
                loanApplication.getLoanAmount(),
                loanApplication.getInterestRate(),
                loanApplication.getTenureMonths(),
                loanApplication.getLoanPurpose(),
                loanApplication.getStatus(),
                loanApplication.getCreatedAt(),
                loanApplication.getUpdatedAt()
        );
    }
}
