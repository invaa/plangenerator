package com.lendico.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class Error {
    private final LocalDateTime timestamp;
    private final int status;
    private final String message;
}
