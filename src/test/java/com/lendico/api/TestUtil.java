package com.lendico.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.lendico.api.controller.ExceptionHandlerConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.annotation.ExceptionHandlerMethodResolver;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod;

import java.lang.reflect.Method;
import java.util.Collections;

@Slf4j
public final class TestUtil {
    public static HandlerExceptionResolver getExceptionResolver() {
        ExceptionHandlerExceptionResolver resolver = new ExceptionHandlerExceptionResolver() {
            @Override
            protected ServletInvocableHandlerMethod getExceptionHandlerMethod(HandlerMethod handlerMethod, Exception exception) {
                Method method = new ExceptionHandlerMethodResolver(ExceptionHandlerConfiguration.class).resolveMethod(exception);

                log.debug(String.format("Handling exception %s by %s", exception.getMessage(), method.toString()));
                return new ServletInvocableHandlerMethod(new ExceptionHandlerConfiguration(), method);
            }
        };
        resolver.setMessageConverters(Collections.singletonList(new MappingJackson2HttpMessageConverter()));
        resolver.afterPropertiesSet();
        return resolver;
    }

    public static String convertToJsonString(Object o) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new Jdk8Module());
        return objectMapper.writeValueAsString(o);
    }
}
