package com.lendico.api.exception;

public class InvalidPlanParametersException extends RuntimeException {
    public InvalidPlanParametersException(final String message) {
	    super(message);
    }
}
