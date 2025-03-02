package com.gabriel.rentacar.exception.accountException;

import com.gabriel.rentacar.exception.ValidationException;

public class AccountPhoneNumberAlreadyExistsException extends ValidationException {
  public AccountPhoneNumberAlreadyExistsException(String phoneNumber) {
    super(String.format("Phone number already exists: %s", phoneNumber),
            "This phone number is already registered with another account");
  }
}