package com.gabriel.rentacar.exception;

import lombok.Getter;

@Getter
public class ValidationException extends RuntimeException {
	private final String clientMessage;
	public ValidationException(String message, String clientMessage) {
		super(message);
		this.clientMessage = clientMessage;
	}

}