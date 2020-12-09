package com.lendico.api.service;

import com.lendico.api.config.ApplicationConfig;
import com.lendico.api.dto.BorrowerPayment;
import com.lendico.api.dto.GenerationParameters;
import com.lendico.api.dto.RepaymentPlan;
import com.lendico.api.exception.InvalidPlanParametersException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class PlanGenerationServiceTest {

	private static final ApplicationConfig config = new ApplicationConfig();
	private static final CalendarService calendarService = new CalendarService();
	static {
		ReflectionTestUtils.setField(config, "divisionPrecision", 8);
		ReflectionTestUtils.setField(config, "resultRounding", 2);
	}

	@Test
	public void shouldGeneratePlanFor24MonthFrom01012018() {
		//given
		final GenerationParameters expectedDto = new GenerationParameters(
				new BigDecimal("5000"),
				new BigDecimal("5.0"),
				24,
				LocalDateTime.of(2018,1,1,0,0,1)
		);
		final PlanGenerationService planGenerationService = new PlanGenerationService(config, calendarService);

		//when
		final RepaymentPlan actualRepaymentPlan = planGenerationService.generate(expectedDto);

		//then
		validatePlan(actualRepaymentPlan, expectedDto);
	}

	@Test
	public void shouldGeneratePlansAndInterestPlusPrincipalEqualsAnnuityMultiplyDuration() {
		//given
		final PlanGenerationService planGenerationService = new PlanGenerationService(config, calendarService);
		final GenerationParameters expectedDto = new GenerationParameters(
				new BigDecimal("5000"),
				new BigDecimal("5.0"),
				24,
				LocalDateTime.of(2018,1,1,0,0,1)
		);

		//when
		final RepaymentPlan actualRepaymentPlan = planGenerationService.generate(expectedDto);

		//then
		validatePlan(actualRepaymentPlan, expectedDto);
	}

	@Test
	public void shouldThrowExceptionWhenInterestIsGreaterThanAnnuity() {
		//given
		final PlanGenerationService planGenerationService = new PlanGenerationService(config, calendarService);
		final GenerationParameters expectedDto = new GenerationParameters(
				new BigDecimal("5000"),
				new BigDecimal("5.0"),
				2400,
				LocalDateTime.of(2018,1,1,0,0,1)
		);

		//when
		final Throwable throwable = catchThrowable(() -> planGenerationService.generate(expectedDto));

		//then
		assertThat(throwable)
				.isNotNull()
				.isInstanceOf(InvalidPlanParametersException.class)
				.hasMessageContaining("Annuity is greater than monthly interest");
	}

	private void validatePlan(RepaymentPlan actualRepaymentPlan, GenerationParameters parameters) {
		BigDecimal totalPrincipal = ZERO;
		BigDecimal totalInterest = ZERO;
		BigDecimal totalAnnuity = ZERO;
		for (BorrowerPayment borrowerPayment: actualRepaymentPlan.getBorrowerPayments()) {
			assertEquals(borrowerPayment.getBorrowerPaymentAmount(), borrowerPayment.getInterest().add(borrowerPayment.getPrincipal()));
			totalPrincipal = totalPrincipal.add(borrowerPayment.getPrincipal());
			totalInterest = totalInterest.add(borrowerPayment.getInterest());
			totalAnnuity = totalAnnuity.add(borrowerPayment.getBorrowerPaymentAmount());
		}
		assertEquals(0, totalPrincipal.compareTo(parameters.getLoanAmount()));
		assertEquals(0, totalAnnuity.compareTo(totalPrincipal.add(totalInterest)));
		assertEquals(java.util.Optional.of(actualRepaymentPlan.getBorrowerPayments().size()).get(), parameters.getDuration());
	}

	@Test
	public void shouldGeneratePlanAndLastRemainingProncipalShouldBeZero() {
		//given
		final GenerationParameters expectedDto = new GenerationParameters(
				new BigDecimal("5000"),
				new BigDecimal("5.0"),
				1,
				LocalDateTime.of(2018,1,1,0,0,1)
		);
		final PlanGenerationService planGenerationService = new PlanGenerationService(config, calendarService);

		//when
		final RepaymentPlan actualRepaymentPlan = planGenerationService.generate(expectedDto);

		//then
		assertEquals(0, actualRepaymentPlan.getBorrowerPayments().get(0).getRemainingOutstandingPrincipal().compareTo(ZERO));
	}

	@Test
	public void shouldGeneratePlanAndInterestIsLoweredWhenAnnuityIsLessThanInterestPlusPrincipal() {
		//given
		final GenerationParameters expectedDto = new GenerationParameters(
				new BigDecimal("50000"),
				new BigDecimal("6.0"),
				24,
				LocalDateTime.of(2018,1,1,0,0,1)
		);
		final PlanGenerationService planGenerationService = new PlanGenerationService(config, calendarService);

		//when
		final RepaymentPlan actualRepaymentPlan = planGenerationService.generate(expectedDto);

		//then
		validatePlan(actualRepaymentPlan, expectedDto);
		final BorrowerPayment lastBorrowerPayment = actualRepaymentPlan.getBorrowerPayments().get(expectedDto.getDuration() - 1);
		assertEquals(0, lastBorrowerPayment.getInterest().add(lastBorrowerPayment.getPrincipal()).subtract(lastBorrowerPayment.getBorrowerPaymentAmount()).compareTo(ZERO));
	}
}