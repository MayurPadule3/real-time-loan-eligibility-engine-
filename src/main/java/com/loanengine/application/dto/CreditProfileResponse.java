
package com.loanengine.application.dto;

import com.loanengine.application.entity.CreditDataSource;
import com.loanengine.application.entity.CreditProfile;
import com.loanengine.application.entity.EmploymentType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record CreditProfileResponse(
        Long id,
        Long customerId,
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
        LocalDate asOfDate,
        LocalDateTime createdAt
) {

    public static CreditProfileResponse from(CreditProfile profile) {
        return new CreditProfileResponse(
                profile.getId(),
                profile.getCustomer().getId(),
                profile.getMonthlyIncome(),
                profile.getMonthlyDebtObligations(),
                profile.getEmploymentType(),
                profile.getRevolvingCreditLimit(),
                profile.getRevolvingCreditBalance(),
                profile.getTotalPaymentsLast12Months(),
                profile.getOnTimePaymentsLast12Months(),
                profile.getSecuredCreditAccounts(),
                profile.getUnsecuredCreditAccounts(),
                profile.getCreditHistoryMonths(),
                profile.getDataSource(),
                profile.getAsOfDate(),
                profile.getCreatedAt()
        );
    }
}
