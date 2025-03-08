package com.gabriel.rentacar.utils;

import com.gabriel.rentacar.exception.accountException.AccountInvalidDataException;
import com.gabriel.rentacar.exception.accountException.AccountInvalidEmailFormatException;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.stereotype.Component;

@Component
public class EmailValidation {

	public String validateEmailFormatAndNormalize(String email) {
		// null or empty email
		if (email == null || email.trim().isEmpty()) {
			throw new AccountInvalidDataException("email", "Email cannot be empty");
		}

		// Normalize the email
		String normalizedEmail = email.trim().toLowerCase();
		EmailValidator emailValidator = EmailValidator.getInstance();

		// basic validity using Apache Commons Validator
		if (!emailValidator.isValid(normalizedEmail)) {
			throw new AccountInvalidEmailFormatException(normalizedEmail);
		}

		String[] parts = normalizedEmail.split("@");
		if (parts.length != 2) {
			throw new AccountInvalidEmailFormatException("Email must contain exactly one '@' symbol: " + normalizedEmail);
		}

		String localPart = parts[0];
		String domainPart = parts[1];

		// empty local and domain parts
		if (localPart.isEmpty() || domainPart.isEmpty()) {
			throw new AccountInvalidEmailFormatException("Local and domain parts cannot be empty: " + normalizedEmail);
		}


		//valid first and last char of each local and domain
		char firstCharLocal = localPart.charAt(0);
		char lastCharLocal = localPart.charAt(localPart.length() - 1);
		if (isInvalidCharacter(firstCharLocal) || isInvalidCharacter(lastCharLocal)) {
			throw new AccountInvalidEmailFormatException("Local part cannot start or end with invalid characters: " + normalizedEmail);
		}

		char firstCharDomain = domainPart.charAt(0);
		char lastCharDomain = domainPart.charAt(domainPart.length() - 1);
		if (isInvalidCharacter(firstCharDomain) || isInvalidCharacter(lastCharDomain)) {
			throw new AccountInvalidEmailFormatException("Domain part cannot start or end with invalid characters: " + normalizedEmail);
		}

		// consecutive dots in local part
		if (localPart.contains("..")) {
			throw new AccountInvalidEmailFormatException("Local part cannot contain consecutive dots: " + normalizedEmail);
		}

		// consecutive dots in domain part
		if (domainPart.contains("..")) {
			throw new AccountInvalidEmailFormatException("Domain part cannot contain consecutive dots: " + normalizedEmail);
		}

		return normalizedEmail;
	}

	private boolean isInvalidCharacter(char c) {
		// invalid characters
		return c == '.' || c == '#' || c == '@' || c == ' ' || c == '-' || c == '_';
	}
}