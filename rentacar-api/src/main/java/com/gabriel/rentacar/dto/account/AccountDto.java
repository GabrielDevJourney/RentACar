package com.gabriel.rentacar.dto.account;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {
	@NotBlank(message = "Must have first name")
	@Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
	@Pattern(regexp = "^[a-zA-Z\\s'-]+$",
			message = "First name can only contain letters, spaces, hyphens and apostrophes")
	private String firstName;

	@NotBlank(message = "Must have last name")
	@Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
	@Pattern(regexp = "^[a-zA-Z\\s'-]+$",
			message = "Last name can only contain letters, spaces, hyphens and apostrophes")
	private String lastName;

	@NotEmpty(message = "Must have email")
	@Email(message = "Email format is invalid")
	@Size(max = 100, message = "Email must be less than 100 characters")
	@Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
			message = "Email format is invalid")
	private String email;

	@NotBlank(message = "Must have phone number")
	@Pattern(regexp = "^(91|92|93|96)\\d{7}$",
			message = "Invalid phone number format. Expected format:91 or 92 or 93 or 96.")
	private String phoneNumber;

	@NotNull(message = "Must have age")
	@Range(min = 18, max = 100, message = "Age must be between 18 and 100")
	private Integer age;
}