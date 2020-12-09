package com.lendico.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BorrowerPayment {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final BigDecimal borrowerPaymentAmount;
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private final LocalDateTime date;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final BigDecimal initialOutstandingPrincipal;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final BigDecimal interest;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final BigDecimal principal;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final BigDecimal remainingOutstandingPrincipal;
}
