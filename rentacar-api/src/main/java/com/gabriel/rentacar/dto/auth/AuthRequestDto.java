package com.gabriel.rentacar.dto.auth;

import com.gabriel.rentacar.enums.UserRole;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequestDto {
	@NotEmpty(message = "Must have email")
	@Email(message = "Email format is invalid")
	@Size(max = 100, message = "Email must be less than 100 characters")
	@Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
			message = "Email format is invalid")
	private String email;

	@NotBlank(message = "Password is required")
	@Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
	@Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$",
			message = "Password must contain at least one digit, one lowercase letter, one uppercase letter, and one special character")
	private String password;

}
