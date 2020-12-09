package com.lendico.api.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
public class GenerationParametersTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	{
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
	}

	@Test
	public void shouldDeserializeFromString() throws IOException {
		//given
		final String serializedJson = "{\n" + " \"loanAmount\": \"5000\",\n" + " \"nominalRate\": \"5.0\",\n" + " \"duration\": 24,\n" + " \"startDate\": \"2018-01-01T00:00:01Z\"\n" + "}";
		final GenerationParameters expectedDto = new GenerationParameters(
				new BigDecimal("5000"),
				new BigDecimal("5.0"),
				24,
				LocalDateTime.of(2018,1,1,0,0,1)
		);

		//when
		final GenerationParameters actualDto = objectMapper.readValue(serializedJson, GenerationParameters.class);

		//then
		assertThat(reflectionEquals(expectedDto, actualDto)).isTrue();
	}
}