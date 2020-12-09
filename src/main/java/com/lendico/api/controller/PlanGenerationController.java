package com.lendico.api.controller;

import com.lendico.api.dto.GenerationParameters;
import com.lendico.api.dto.RepaymentPlan;
import com.lendico.api.service.PlanGenerationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping(value = "generate-plan")
@Slf4j
@Validated
public class PlanGenerationController {

    private final PlanGenerationService planGenerationService;

    public PlanGenerationController(final PlanGenerationService planGenerationService) {
        this.planGenerationService = planGenerationService;
    }

    @Cacheable(value = "plans", key = "{ #root.methodName, #parameters?.loanAmount, #parameters?.nominalRate, #parameters?.duration, #parameters?.startDate }")
    @RequestMapping(method = POST, value = "", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(OK)
    public RepaymentPlan generatePlan(final @Valid @RequestBody GenerationParameters parameters) {
        return planGenerationService.generate(parameters);
    }
}
