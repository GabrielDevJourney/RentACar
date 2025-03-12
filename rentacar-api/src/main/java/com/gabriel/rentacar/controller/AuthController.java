package com.gabriel.rentacar.controller;

import com.gabriel.rentacar.dto.auth.AuthRequestDto;
import com.gabriel.rentacar.dto.auth.AuthResponseDto;
import com.gabriel.rentacar.dto.auth.ChangePasswordDto;
import com.gabriel.rentacar.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody AuthRequestDto authRequest) {
		AuthResponseDto response = authService.authenticate(authRequest);
		return ResponseEntity.ok(response);
	}

	@PatchMapping("/change-password/{accountId}")
	public ResponseEntity<Void> changePassword(
			@PathVariable Long accountId,
			@Valid @RequestBody ChangePasswordDto passwordDto) {
		authService.changePassword(accountId, passwordDto);
		return ResponseEntity.ok().build();
	}
}