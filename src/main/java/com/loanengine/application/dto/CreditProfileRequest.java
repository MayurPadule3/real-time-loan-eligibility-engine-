
package com.loanengine.application.dto;

import com.loanengine.application.entity.EmploymentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreditProfileRequest(

        @NotNull
        @Positive
        Long customerId,

        @NotNull
        @DecimalMin("0.01")
        BigDecimal monthlyIncome,

        @NotNull
        @DecimalMin("0.00")
        BigDecimal monthlyDebtObligations,

        @NotNull
        EmploymentType employmentType,

        @NotNull
        @DecimalMin("0.00")
        BigDecimal revolvingCreditLimit,

        @NotNull
        @DecimalMin("0.00")
        BigDecimal revolvingCreditBalance,

        @NotNull
        @Min(0)
        Integer totalPaymentsLast12Months,

        @NotNull
        @Min(0)
        Integer onTimePaymentsLast12Months,

        @NotNull
        @Min(0)
        Integer securedCreditAccounts,

        @NotNull
        @Min(0)
        Integer unsecuredCreditAccounts,

        @NotNull
        @Min(0)
        Integer creditHistoryMonths,

        @NotNull
        @PastOrPresent
        LocalDate asOfDate

) {}
