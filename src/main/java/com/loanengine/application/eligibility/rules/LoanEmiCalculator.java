
package com.loanengine.application.eligibility.rules;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

@Component
public class LoanEmiCalculator {

    private static final MathContext MATH_CONTEXT =
            MathContext.DECIMAL128;

    private static final BigDecimal MONTHS_IN_YEAR =
            new BigDecimal("12");

    private static final BigDecimal PERCENTAGE =
            new BigDecimal("100");

    private static final int MONEY_SCALE = 2;

    /**
     * Calculates monthly EMI using the reducing balance method.
     *
     * @param loanAmount principal amount
     * @param annualInterestRate annual interest rate in percentage
     * @param tenureMonths loan tenure in months
     * @return monthly EMI rounded to two decimal places
     */
    public BigDecimal calculateMonthlyEmi(
            BigDecimal loanAmount,
            BigDecimal annualInterestRate,
            Integer tenureMonths) {

        validateInputs(loanAmount, annualInterestRate, tenureMonths);

        // Handle zero-interest loans separately.
        if (annualInterestRate.compareTo(BigDecimal.ZERO) == 0) {

            return loanAmount.divide(
                    BigDecimal.valueOf(tenureMonths),
                    MONEY_SCALE,
                    RoundingMode.HALF_UP
            );
        }

        // Convert annual percentage rate into monthly decimal rate.
        BigDecimal monthlyInterestRate = annualInterestRate.divide(
                PERCENTAGE.multiply(MONTHS_IN_YEAR),
                MATH_CONTEXT
        );

        // Calculate (1 + r)^n.
        BigDecimal onePlusRate = BigDecimal.ONE.add(
                monthlyInterestRate,
                MATH_CONTEXT
        );

        BigDecimal power = onePlusRate.pow(
                tenureMonths,
                MATH_CONTEXT
        );

        // EMI = P * r * (1 + r)^n / ((1 + r)^n - 1)
        BigDecimal numerator = loanAmount
                .multiply(monthlyInterestRate, MATH_CONTEXT)
                .multiply(power, MATH_CONTEXT);

        BigDecimal denominator = power.subtract(
                BigDecimal.ONE,
                MATH_CONTEXT
        );

        return numerator.divide(
                denominator,
                MONEY_SCALE,
                RoundingMode.HALF_UP
        );
    }

    private void validateInputs(
            BigDecimal loanAmount,
            BigDecimal annualInterestRate,
            Integer tenureMonths) {

        if (loanAmount == null ||
                loanAmount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Loan amount must be greater than zero"
            );
        }

        if (annualInterestRate == null ||
                annualInterestRate.compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "Annual interest rate cannot be negative or null"
            );
        }

        if (tenureMonths == null || tenureMonths <= 0) {

            throw new IllegalArgumentException(
                    "Loan tenure must be greater than zero"
            );
        }
    }
}
