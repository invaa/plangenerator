package com.lendico.api.config;

import com.ulisesbocchio.jasyptspringboot.annotation.EncryptablePropertySource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@EnableCaching
@EncryptablePropertySource("classpath:application.properties")
@ComponentScan({"com.lendico.api.controller", "com.lendico.api.service"})
public class ApplicationConfig {

    @Getter
    @Value("${plan.generator.division.precision:8}")
    private int divisionPrecision;

    @Getter
    @Value("${plan.generator.result.rounding:2}")
    private int resultRounding;

}
