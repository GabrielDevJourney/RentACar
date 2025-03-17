package com.gabriel.rentacar.dto.account;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountConfirmationDto {
	@Email
	private String email;
	private String password;
}
