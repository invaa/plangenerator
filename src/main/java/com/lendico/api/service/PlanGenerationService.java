package com.lendico.api.service;

import com.lendico.api.config.ApplicationConfig;
import com.lendico.api.dto.BorrowerPayment;
import com.lendico.api.dto.GenerationParameters;
import com.lendico.api.dto.RepaymentPlan;
import com.lendico.api.exception.InvalidPlanParametersException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PlanGenerationService {
    private static final BigDecimal BIG_DECIMAL_ONE_HUNDRED = new BigDecimal(100);
    private static final int MONTHS_IN_A_YEAR = 12;
    private final ApplicationConfig config;
    private final CalendarService calendarService;
    private final MathContext mathContext;

    @Autowired
    public PlanGenerationService(
            @Qualifier("applicationConfig") final ApplicationConfig config,
            final CalendarService calendarService
    ) {
        this.config = config;
        this.calendarService = calendarService;
        this.mathContext = new MathContext(config.getDivisionPrecision(), RoundingMode.HALF_UP);
    }

    public RepaymentPlan generate(final GenerationParameters parameters) {
        final BigDecimal monthlyInterestRate = getMonthlyInterestRate(parameters.getNominalRate());
        BigDecimal annuity = getAnnuity(parameters, monthlyInterestRate);
        BigDecimal currentOutstandingPrincipal = parameters.getLoanAmount()
                .setScale(config.getResultRounding(), RoundingMode.HALF_UP);
        BigDecimal currentInterest = getCurrentInterest(parameters, currentOutstandingPrincipal);

        if (currentInterest.compareTo(annuity) >= 0) {
            throw new InvalidPlanParametersException("Annuity is greater than monthly interest, plan can't be generated.");
        }

        BigDecimal currentPrincipal = annuity.subtract(currentInterest);
        int currentMonth = parameters.getStartDate().getMonthValue();
        int currentYear = parameters.getStartDate().getYear();
        List<BorrowerPayment> borrowerPayments = new ArrayList<>();

        for (int i = 0; i < parameters.getDuration(); i++) {
            BigDecimal remainingOutstandingPrincipal = getRemainingOutstandingPrincipal(currentOutstandingPrincipal, currentPrincipal);
            final boolean isLastIteration = i == parameters.getDuration() - 1;

            if (isLastIteration) {
                if (remainingOutstandingPrincipal.compareTo(BigDecimal.ZERO) > 0) {
                    currentPrincipal = remainingOutstandingPrincipal.add(currentPrincipal);
                    remainingOutstandingPrincipal = BigDecimal.ZERO;
                }
                if (currentPrincipal.compareTo(currentOutstandingPrincipal) > 0) {
                    annuity = annuity.add(currentOutstandingPrincipal).subtract(currentPrincipal);
                    currentPrincipal = currentOutstandingPrincipal;
                }
                if (currentInterest.add(currentPrincipal).compareTo(annuity) > 0) {
                    annuity = currentInterest.add(currentPrincipal);
                }
                if (remainingOutstandingPrincipal.compareTo(BigDecimal.ZERO) == 0) {
                    remainingOutstandingPrincipal = BigDecimal.ZERO;
                }
            }

            borrowerPayments.add(
                    buildBorrowerPayment(
                            annuity,
                            currentOutstandingPrincipal,
                            currentInterest,
                            currentPrincipal,
                            currentMonth,
                            currentYear,
                            remainingOutstandingPrincipal
                    )
            );

            if (currentMonth == 12) {
                currentYear++;
                currentMonth = 1;
            } else {
                currentMonth++;
            }

            currentOutstandingPrincipal = remainingOutstandingPrincipal;
            currentInterest = getCurrentInterest(parameters, currentOutstandingPrincipal);
            currentPrincipal = annuity.subtract(currentInterest);
        }
        return new RepaymentPlan(borrowerPayments);
    }

    @Cacheable("annuities")
    public BigDecimal getAnnuity(
            final GenerationParameters parameters,
            final BigDecimal monthlyInterestRate
    ) {
        return parameters
                .getLoanAmount()
                .multiply(monthlyInterestRate)
                .divide(BigDecimal.ONE
                        .subtract(BigDecimal.ONE
                                .add(monthlyInterestRate).pow(-1 * parameters.getDuration(), mathContext)
                        ), mathContext
                )
                .setScale(config.getResultRounding(), RoundingMode.HALF_UP);
    }

    private BigDecimal getMonthlyInterestRate(final BigDecimal nominalRate) {
        return nominalRate
                .divide(BIG_DECIMAL_ONE_HUNDRED, mathContext)
                .divide(new BigDecimal(MONTHS_IN_A_YEAR), mathContext);
    }

    private BigDecimal getRemainingOutstandingPrincipal(
            final BigDecimal currentOutstandingPrincipal,
            final BigDecimal currentPrincipal
    ) {
        return currentOutstandingPrincipal
                .subtract(currentPrincipal
                        .compareTo(currentOutstandingPrincipal) > 0 ? currentOutstandingPrincipal : currentPrincipal);
    }

    private BorrowerPayment buildBorrowerPayment(
            final BigDecimal annuity,
            final BigDecimal currentOutstandingPrincipal,
            final BigDecimal currentInterest,
            final BigDecimal currentPrincipal,
            final int currentMonth,
            final int currentYear,
            final BigDecimal remainingOutstandingPrincipal
    ) {
        return new BorrowerPayment(
                annuity,
                LocalDateTime.of(currentYear, currentMonth, 1, 0, 0, 0),
                currentOutstandingPrincipal,
                currentInterest,
                currentPrincipal,
                remainingOutstandingPrincipal
        );
    }

    private BigDecimal getCurrentInterest(
            final GenerationParameters parameters,
            final BigDecimal currentOutstandingPrincipal
    ) {
        return parameters.getNominalRate()
                .divide(BIG_DECIMAL_ONE_HUNDRED, mathContext)
                .multiply(new BigDecimal(calendarService.daysInMonth()))
                .multiply(currentOutstandingPrincipal)
                .divide(new BigDecimal(calendarService.getDaysInYear()), mathContext)
                .setScale(config.getResultRounding(), RoundingMode.HALF_UP);
    }
}
