
package com.loanengine.application.scoring.service;

import com.loanengine.application.entity.CreditProfile;
import com.loanengine.application.scoring.CreditScoreCalculator;
import com.loanengine.application.scoring.CreditScoreResult;
import com.loanengine.application.service.CreditProfileService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreditScoringService {

    private final CreditScoreCalculator creditScoreCalculator;
    private final CreditProfileService creditProfileService;

    public CreditScoringService(
            CreditScoreCalculator creditScoreCalculator,
            CreditProfileService creditProfileService) {

        this.creditScoreCalculator = creditScoreCalculator;
        this.creditProfileService = creditProfileService;
    }

    // Retrieve customer's latest credit profile
    @Transactional(readOnly = true)
    public CreditProfile getLatestCreditProfile(Long customerId) {

        return creditProfileService.getLatestCreditProfile(customerId);
    }

    // Calculate credit score using the supplied profile
    @Transactional(readOnly = true)
    public CreditScoreResult calculateCreditScore(CreditProfile profile) {

        return creditScoreCalculator.calculate(profile);
    }
}
