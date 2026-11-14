
package com.loanengine.application.eligibility.model;

import com.loanengine.application.scoring.CreditRiskCategory;

import java.math.BigDecimal;
import java.util.List;

public record EligibilityResult(

        EligibilityDecision decision,

        List<EligibilityReason> reasons,

        BigDecimal debtToIncomeRatio,

        Integer creditScore,

        CreditRiskCategory creditRiskCategory,

        String ruleVersion

) {

    public EligibilityResult {
        if (decision == null) {
            throw new IllegalArgumentException(
                    "Eligibility decision cannot be null"
            );
        }

        if (reasons == null || reasons.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one eligibility reason is required"
            );
        }

        reasons = List.copyOf(reasons);

        if (ruleVersion == null || ruleVersion.isBlank()) {
            throw new IllegalArgumentException(
                    "Rule version cannot be empty"
            );
        }
    }
}
