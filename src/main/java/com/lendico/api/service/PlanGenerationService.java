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
    private static final BigDecimal ONE_HUNDRED = new BigDecimal(100);
    private static final int MONTH_IN_A_YEAR = 12;
    private final ApplicationConfig config;
    private final CalendarService calendarService;

    @Autowired
    public PlanGenerationService(
            @Qualifier("applicationConfig") final ApplicationConfig config,
            final CalendarService calendarService
    ) {
        this.config = config;
        this.calendarService = calendarService;
    }

    public RepaymentPlan generate(final GenerationParameters parameters) {
        final MathContext mathContext = new MathContext(config.getDivisionPrecision(), RoundingMode.HALF_UP);

        final BigDecimal monthlyInterestRate = parameters
                .getNominalRate()
                .divide(ONE_HUNDRED, mathContext)
                .divide(new BigDecimal(MONTH_IN_A_YEAR), mathContext);

        BigDecimal annuity = getAnnuity(parameters, mathContext, monthlyInterestRate);

        BigDecimal currentOutstandingPrincipal = parameters.getLoanAmount()
                .setScale(config.getResultRounding(), RoundingMode.HALF_UP);

        BigDecimal currentInterest =
                parameters.getNominalRate()
                        .divide(ONE_HUNDRED, mathContext)
                        .multiply(new BigDecimal(calendarService.daysInMonth()))
                        .multiply(currentOutstandingPrincipal)
                        .divide(new BigDecimal(calendarService.getDaysInYear()), mathContext)
                        .setScale(config.getResultRounding(), RoundingMode.HALF_UP);

        if (currentInterest.compareTo(annuity) >= 0) {
            throw new InvalidPlanParametersException("Annuity is greater than monthly interest, plan can't be generated.");
        }

        BigDecimal currentPrincipal = annuity.subtract(currentInterest);
        int currentMonth = parameters.getStartDate().getMonthValue();
        int currentYear = parameters.getStartDate().getYear();

        List<BorrowerPayment> borrowerPayments = new ArrayList<>();

        for (int i = 0; i < parameters.getDuration(); i++) {
            BigDecimal remainingOutstandingPrincipal = currentOutstandingPrincipal
                    .subtract(currentPrincipal
                            .compareTo(currentOutstandingPrincipal) > 0 ? currentOutstandingPrincipal : currentPrincipal);
            if (i == parameters.getDuration() - 1) {
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

            final BorrowerPayment currentBorrowerPayment = new BorrowerPayment(
                    annuity,
                    LocalDateTime.of(currentYear, currentMonth, 1, 0, 0, 0),
                    currentOutstandingPrincipal,
                    currentInterest,
                    currentPrincipal,
                    remainingOutstandingPrincipal
            );

            borrowerPayments.add(currentBorrowerPayment);

            if (currentMonth == 12) {
                currentYear++;
                currentMonth = 1;
            } else {
                currentMonth++;
            }

            //TODO: refactor
            currentOutstandingPrincipal = remainingOutstandingPrincipal;
            currentInterest =
                    parameters.getNominalRate()
                            .divide(ONE_HUNDRED, mathContext)
                            .multiply(new BigDecimal(calendarService.daysInMonth()))
                            .multiply(currentOutstandingPrincipal)
                            .divide(new BigDecimal(calendarService.getDaysInYear()), mathContext)
                            .setScale(config.getResultRounding(), RoundingMode.HALF_UP);
            currentPrincipal = annuity.subtract(currentInterest);
        }
        return new RepaymentPlan(borrowerPayments);
    }

    @Cacheable("annuities")
    public BigDecimal getAnnuity(final GenerationParameters parameters, final MathContext mathContext, final BigDecimal monthlyInterestRate) {
        return parameters
                .getLoanAmount()
                .multiply(monthlyInterestRate)
                .divide(BigDecimal.ONE.subtract(BigDecimal.ONE.add(monthlyInterestRate).pow(-1 * parameters.getDuration(), mathContext)), mathContext)
                .setScale(config.getResultRounding(), RoundingMode.HALF_UP);
    }
}
