
package com.loanengine.application.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "credit_profiles",
        indexes = {
                @Index(
                        name = "idx_credit_profiles_customer_as_of_date",
                        columnList = "customer_id, as_of_date"
                )
        }
)
public class CreditProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // A customer can have multiple credit profile snapshots.
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    // Applicant financial information
    @NotNull
    @DecimalMin("0.01")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyIncome;

    @NotNull
    @DecimalMin("0.00")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyDebtObligations;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EmploymentType employmentType;

    // Credit utilization information
    @NotNull
    @DecimalMin("0.00")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal revolvingCreditLimit;

    @NotNull
    @DecimalMin("0.00")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal revolvingCreditBalance;

    // Payment history for the last 12 months
    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Integer totalPaymentsLast12Months;

    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Integer onTimePaymentsLast12Months;

    // Credit account information
    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Integer securedCreditAccounts;

    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Integer unsecuredCreditAccounts;

    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Integer creditHistoryMonths;

    // Snapshot metadata
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CreditDataSource dataSource = CreditDataSource.SYNTHETIC_DEMO;

    @NotNull
    @Column(name = "as_of_date", nullable = false)
    private LocalDate asOfDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected CreditProfile() {
        // Required by JPA
    }

    public CreditProfile(
            Customer customer,
            BigDecimal monthlyIncome,
            BigDecimal monthlyDebtObligations,
            EmploymentType employmentType,
            BigDecimal revolvingCreditLimit,
            BigDecimal revolvingCreditBalance,
            Integer totalPaymentsLast12Months,
            Integer onTimePaymentsLast12Months,
            Integer securedCreditAccounts,
            Integer unsecuredCreditAccounts,
            Integer creditHistoryMonths,
            CreditDataSource dataSource,
            LocalDate asOfDate) {

        this.customer = customer;
        this.monthlyIncome = monthlyIncome;
        this.monthlyDebtObligations = monthlyDebtObligations;
        this.employmentType = employmentType;
        this.revolvingCreditLimit = revolvingCreditLimit;
        this.revolvingCreditBalance = revolvingCreditBalance;
        this.totalPaymentsLast12Months = totalPaymentsLast12Months;
        this.onTimePaymentsLast12Months = onTimePaymentsLast12Months;
        this.securedCreditAccounts = securedCreditAccounts;
        this.unsecuredCreditAccounts = unsecuredCreditAccounts;
        this.creditHistoryMonths = creditHistoryMonths;
        this.dataSource = dataSource;
        this.asOfDate = asOfDate;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public BigDecimal getMonthlyIncome() {
        return monthlyIncome;
    }

    public BigDecimal getMonthlyDebtObligations() {
        return monthlyDebtObligations;
    }

    public EmploymentType getEmploymentType() {
        return employmentType;
    }

    public BigDecimal getRevolvingCreditLimit() {
        return revolvingCreditLimit;
    }

    public BigDecimal getRevolvingCreditBalance() {
        return revolvingCreditBalance;
    }

    public Integer getTotalPaymentsLast12Months() {
        return totalPaymentsLast12Months;
    }

    public Integer getOnTimePaymentsLast12Months() {
        return onTimePaymentsLast12Months;
    }

    public Integer getSecuredCreditAccounts() {
        return securedCreditAccounts;
    }

    public Integer getUnsecuredCreditAccounts() {
        return unsecuredCreditAccounts;
    }

    public Integer getCreditHistoryMonths() {
        return creditHistoryMonths;
    }

    public CreditDataSource getDataSource() {
        return dataSource;
    }

    public LocalDate getAsOfDate() {
        return asOfDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
