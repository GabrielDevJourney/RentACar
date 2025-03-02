package com.gabriel.rentacar.utils;

import com.gabriel.rentacar.exception.accountException.AccountInvalidDataException;
import com.gabriel.rentacar.exception.accountException.AccountInvalidEmailFormatException;
import org.apache.commons.validator.routines.EmailValidator;

public class EmailValidation {

	public String validateEmailFormatAndNormalize(String email) {
		// Check for null or empty email
		if (email == null || email.trim().isEmpty()) {
			throw new AccountInvalidDataException("email", "Email cannot be empty");
		}

		// Normalize the email
		String normalizedEmail = email.trim().toLowerCase();
		EmailValidator emailValidator = EmailValidator.getInstance();

		// Check basic validity using Apache Commons Validator
		if (!emailValidator.isValid(normalizedEmail)) {
			throw new AccountInvalidEmailFormatException(normalizedEmail);
		}

		// Split the email into local and domain parts
		String[] parts = normalizedEmail.split("@");
		if (parts.length != 2) {
			throw new AccountInvalidEmailFormatException("Email must contain exactly one '@' symbol: " + normalizedEmail);
		}

		String localPart = parts[0];
		String domainPart = parts[1];

		// Check for empty local and domain parts
		if (localPart.isEmpty() || domainPart.isEmpty()) {
			throw new AccountInvalidEmailFormatException("Local and domain parts cannot be empty: " + normalizedEmail);
		}

		// Check first and last characters of the local part
		char firstCharLocal = localPart.charAt(0);
		char lastCharLocal = localPart.charAt(localPart.length() - 1);
		if (!isValidCharacter(firstCharLocal) || !isValidCharacter(lastCharLocal)) {
			throw new AccountInvalidEmailFormatException("Local part cannot start or end with invalid characters: " + normalizedEmail);
		}

		// Check first and last characters of the domain part
		char firstCharDomain = domainPart.charAt(0);
		char lastCharDomain = domainPart.charAt(domainPart.length() - 1);
		if (!isValidCharacter(firstCharDomain) || !isValidCharacter(lastCharDomain)) {
			throw new AccountInvalidEmailFormatException("Domain part cannot start or end with invalid characters: " + normalizedEmail);
		}

		// Check for consecutive dots in local part
		if (localPart.contains("..")) {
			throw new AccountInvalidEmailFormatException("Local part cannot contain consecutive dots: " + normalizedEmail);
		}

		// Check for consecutive dots in domain part
		if (domainPart.contains("..")) {
			throw new AccountInvalidEmailFormatException("Domain part cannot contain consecutive dots: " + normalizedEmail);
		}

		return normalizedEmail;
	}

	private boolean isValidCharacter(char c) {
		// Define invalid characters
		return c != '.' && c != '#' && c != '@' && c != ' ' && c != '-' && c != '_';
	}
}