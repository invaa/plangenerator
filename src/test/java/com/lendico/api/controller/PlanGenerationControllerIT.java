package com.lendico.api.controller;

import com.lendico.api.config.TestConfig;
import com.lendico.api.dto.BorrowerPayment;
import com.lendico.api.dto.GenerationParameters;
import com.lendico.api.dto.RepaymentPlan;
import com.lendico.api.service.PlanGenerationService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static com.lendico.api.TestUtil.convertToJsonString;
import static com.lendico.api.TestUtil.getExceptionResolver;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
@WebAppConfiguration
public class PlanGenerationControllerIT {

    @Autowired
    @Qualifier("planGenerationServiceMock")
    private PlanGenerationService planGenerationServiceMock;

    @Autowired
    private PlanGenerationController planGenerationController;

    private MockMvc mockMvc;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(planGenerationController)
                .setHandlerExceptionResolvers(getExceptionResolver())
                .build();
    }

    @After
    public void resetMocks() {
        Mockito.reset(planGenerationServiceMock);
    }

    @Test
    public void shouldGenerateRepaymentPlan() throws Exception {
        // given
        final RepaymentPlan plan = new RepaymentPlan(
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
                LocalDateTime.of(2018,1,1,0,0,1)
        );
        when(planGenerationServiceMock.generate(parameters)).thenReturn(plan);

        // when
        final MvcResult mvcResult = mockMvc.perform(post("/generate-plan")
                .content("{ \"loanAmount\": \"5000\",\n" + " \"nominalRate\": \"5.0\",\n" + " \"duration\": 24,\n" + " \"startDate\": \"2018-01-01T00:00:01Z\" }")
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        String content = response.getContentAsString();

        // then
        verify(planGenerationServiceMock).generate(parameters);
        assertEquals(content, "{\"borrowerPayments\":[{\"borrowerPaymentAmount\":\"2216.03\",\"date\":\"2018-01-01T00:00:00Z\",\"initialOutstandingPrincipal\":\"50000.00\",\"interest\":\"250.00\",\"principal\":\"1966.03\",\"remainingOutstandingPrincipal\":\"48033.97\"}]}");
    }

    @Test
    public void shouldThrowBadRequestWhenStartDateEmpty() throws Exception {
        // given
        final RepaymentPlan plan = new RepaymentPlan(
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
                null
        );

        when(planGenerationServiceMock.generate(parameters)).thenReturn(plan);

        // when then
        mockMvc.perform(post("/generate-plan")
                .content(convertToJsonString(parameters))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
    }
}
