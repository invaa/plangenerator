package com.lendico.api.config;

import com.lendico.api.controller.PlanGenerationController;
import com.lendico.api.service.PlanGenerationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;

@Configuration
@ComponentScan({"com.lendico.api.controller", "com.lendico.api.service"})
public class TestConfig {
    @Bean
    public PlanGenerationController planGenerationController(final PlanGenerationService planGenerationService) {
        return new PlanGenerationController(planGenerationService);
    }

    @Primary
    @Bean(name = "planGenerationServiceMock")
    public PlanGenerationService planGenerationServiceMock() {
        return mock(PlanGenerationService.class);
    }

    @Primary
    @Bean(name = "applicationConfig")
    public ApplicationConfig applicationConfigMock() {
        return mock(ApplicationConfig.class);
    }
}
