
package com.loanengine.application.scoring;

import java.math.BigDecimal;

public record CreditScoreResponse(

        Long customerId,

        Long creditProfileId,

        int creditScore,

        CreditRiskCategory riskCategory,

        BigDecimal paymentHistoryScore,

        BigDecimal creditUtilizationScore,

        BigDecimal debtToIncomeScore,

        BigDecimal creditHistoryScore,

        BigDecimal creditMixScore,

        String modelVersion

) {}
