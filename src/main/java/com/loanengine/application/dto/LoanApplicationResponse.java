
package com.loanengine.application.dto;

import com.loanengine.application.entity.LoanStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LoanApplicationResponse {

    private Long id;
    private Long customerId;
    private BigDecimal loanAmount;
    private BigDecimal interestRate;
    private Integer tenureMonths;
    private String loanPurpose;
    private LoanStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public LoanApplicationResponse(
            Long id,
            Long customerId,
            BigDecimal loanAmount,
            BigDecimal interestRate,
            Integer tenureMonths,
            String loanPurpose,
            LoanStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.customerId = customerId;
        this.loanAmount = loanAmount;
        this.interestRate = interestRate;
        this.tenureMonths = tenureMonths;
        this.loanPurpose = loanPurpose;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public BigDecimal getLoanAmount() {
        return loanAmount;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public Integer getTenureMonths() {
        return tenureMonths;
    }

    public String getLoanPurpose() {
        return loanPurpose;
    }

    public LoanStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
