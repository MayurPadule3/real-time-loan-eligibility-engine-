
package com.loanengine.application.eligibility.rules;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class LoanEmiCalculatorTest {

    private LoanEmiCalculator loanEmiCalculator;

    @BeforeEach
    void setUp() {
        loanEmiCalculator = new LoanEmiCalculator();
    }

    @Test
    void shouldCalculateMonthlyEmiSuccessfully() {

        BigDecimal emi = loanEmiCalculator.calculateMonthlyEmi(
                new BigDecimal("500000"),
                new BigDecimal("12"),
                60
        );

        assertEquals(
                new BigDecimal("11122.22"),
                emi
        );
    }

    @Test
    void shouldCalculateEmiForZeroInterestRate() {

        BigDecimal emi = loanEmiCalculator.calculateMonthlyEmi(
                new BigDecimal("120000"),
                BigDecimal.ZERO,
                12
        );

        assertEquals(
                new BigDecimal("10000.00"),
                emi
        );
    }

    @Test
    void shouldRoundEmiToTwoDecimalPlaces() {

        BigDecimal emi = loanEmiCalculator.calculateMonthlyEmi(
                new BigDecimal("100000"),
                new BigDecimal("10"),
                36
        );

        assertEquals(2, emi.scale());
    }

    @Test
    void shouldRejectNullLoanAmount() {

        assertThrows(
                IllegalArgumentException.class,
                () -> loanEmiCalculator.calculateMonthlyEmi(
                        null,
                        new BigDecimal("12"),
                        60
                )
        );
    }

    @Test
    void shouldRejectZeroLoanAmount() {

        assertThrows(
                IllegalArgumentException.class,
                () -> loanEmiCalculator.calculateMonthlyEmi(
                        BigDecimal.ZERO,
                        new BigDecimal("12"),
                        60
                )
        );
    }

    @Test
    void shouldRejectNegativeInterestRate() {

        assertThrows(
                IllegalArgumentException.class,
                () -> loanEmiCalculator.calculateMonthlyEmi(
                        new BigDecimal("500000"),
                        new BigDecimal("-5"),
                        60
                )
        );
    }

    @Test
    void shouldRejectNullInterestRate() {

        assertThrows(
                IllegalArgumentException.class,
                () -> loanEmiCalculator.calculateMonthlyEmi(
                        new BigDecimal("500000"),
                        null,
                        60
                )
        );
    }

    @Test
    void shouldRejectZeroTenure() {

        assertThrows(
                IllegalArgumentException.class,
                () -> loanEmiCalculator.calculateMonthlyEmi(
                        new BigDecimal("500000"),
                        new BigDecimal("12"),
                        0
                )
        );
    }

    @Test
    void shouldRejectNullTenure() {

        assertThrows(
                IllegalArgumentException.class,
                () -> loanEmiCalculator.calculateMonthlyEmi(
                        new BigDecimal("500000"),
                        new BigDecimal("12"),
                        null
                )
        );
    }

    @Test
    void shouldRejectNegativeTenure() {

        assertThrows(
                IllegalArgumentException.class,
                () -> loanEmiCalculator.calculateMonthlyEmi(
                        new BigDecimal("500000"),
                        new BigDecimal("12"),
                        -12
                )
        );
    }
}
