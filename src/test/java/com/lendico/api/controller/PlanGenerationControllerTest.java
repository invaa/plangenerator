package com.lendico.api.controller;

import com.lendico.api.dto.BorrowerPayment;
import com.lendico.api.dto.GenerationParameters;
import com.lendico.api.dto.RepaymentPlan;
import com.lendico.api.exception.InvalidPlanParametersException;
import com.lendico.api.service.PlanGenerationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PlanGenerationControllerTest {

    @Mock
    private PlanGenerationService planGenerationServiceMock;

    @InjectMocks
    private PlanGenerationController planGenerationController;

    @Test
    public void shouldPlayNewRound() {
        // given
		final RepaymentPlan expectedRepaymentPlan = new RepaymentPlan(
				Collections.singletonList(new BorrowerPayment(
						new BigDecimal("2216.03"),
						LocalDateTime.of(2018,1,1,0,0,0),
						new BigDecimal("50000.00"),
						new BigDecimal("250.00"),
						new BigDecimal("1966.03"),
						new BigDecimal("48033.97")
				))
		);
		final GenerationParameters parameters = new GenerationParameters(
				new BigDecimal("5000"),
				new BigDecimal("5.0"),
				24,
				LocalDateTime.of(2018,1,1,0,0,0)
		);
        when(planGenerationServiceMock.generate(parameters)).thenReturn(expectedRepaymentPlan);

        // when
		final RepaymentPlan actualRepaymentPlan = planGenerationController.generatePlan(parameters);

        // then
        assertThat(reflectionEquals(expectedRepaymentPlan, actualRepaymentPlan)).isTrue();
    }

    @Test
    public void shouldThrowInvalidPlanParametersException() {
        // given
		final GenerationParameters parameters = new GenerationParameters(
				new BigDecimal("5000"),
				new BigDecimal("5.0"),
				2400,
				LocalDateTime.of(2018,1,1,0,0,0)
		);
        when(planGenerationServiceMock.generate(parameters)).thenThrow(new InvalidPlanParametersException("Message"));

        // when
        Throwable throwable = catchThrowable(() -> planGenerationController.generatePlan(parameters));

        // then
        assertThat(throwable)
                .isNotNull()
                .isInstanceOf(InvalidPlanParametersException.class)
                .hasMessageContaining("Message");
    }
}
