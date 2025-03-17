package com.gabriel.rentacar.service;


import com.gabriel.rentacar.dto.account.AccountDto;
import com.gabriel.rentacar.dto.auth.AuthRequestDto;
import com.gabriel.rentacar.dto.auth.AuthResponseDto;
import com.gabriel.rentacar.dto.auth.ChangePasswordDto;
import com.gabriel.rentacar.entity.AccountEntity;
import com.gabriel.rentacar.enums.UserRole;
import com.gabriel.rentacar.exception.accountException.AccountInvalidAuthException;
import com.gabriel.rentacar.exception.accountException.AccountInvalidPasswordException;
import com.gabriel.rentacar.exception.accountException.AccountNotActiveException;
import com.gabriel.rentacar.exception.accountException.AccountNotFoundException;
import com.gabriel.rentacar.mapper.AccountMapper;
import com.gabriel.rentacar.repository.AccountRepository;
import com.gabriel.rentacar.utils.EmailValidation;
import com.gabriel.rentacar.utils.JwtTokenUtil;
import com.gabriel.rentacar.utils.PasswordValidation;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {
	private final AccountService accountService;
	private final AccountRepository accountRepository;
	private final AccountMapper accountMapper;
	private final PasswordValidation passwordValidation;
	private final EmailValidation emailValidation;
	private final JwtTokenUtil jwtTokenUtil;

	public AuthService(AccountService accountService,AccountRepository accountRepository, AccountMapper accountMapper,
	                   PasswordValidation passwordValidation, EmailValidation emailValidation,JwtTokenUtil jwtTokenUtil) {
		this.accountService = accountService;
		this.accountRepository = accountRepository;
		this.accountMapper = accountMapper;
		this.passwordValidation = passwordValidation;
		this.emailValidation = emailValidation;
		this.jwtTokenUtil = jwtTokenUtil;
	}

	public void registerAccount(AccountDto accountDto) {
		accountService.createAccount(accountDto);
	}

	public AuthResponseDto authenticate(AuthRequestDto authRequest) {
		String email = emailValidation.validateEmailFormatAndNormalize(authRequest.getEmail());


		AccountEntity account = accountRepository.findByEmail(email)
				.orElseThrow(AccountInvalidAuthException::new);


		String rawPassword = authRequest.getPassword().trim();
		String storedHashedPassword = account.getPassword();


		if (!passwordValidation.matches(rawPassword,storedHashedPassword)) {
			throw new AccountInvalidPasswordException("Invalid password");
		}

		if (!account.isActive()) {
			throw new AccountNotActiveException(account.getId());
		}

		String token = jwtTokenUtil.generateToken(email);

		// Return authenticated user info
		AuthResponseDto responseDto = accountMapper.toAuthResponseDto(account);
		responseDto.setToken(token);
		return responseDto;
	}

	public void changePassword(Long accountId, ChangePasswordDto passwordDto) {
		AccountEntity account = accountRepository.findById(accountId)
				.orElseThrow(() -> new AccountNotFoundException(accountId));

		if (!passwordValidation.matches(passwordDto.getCurrentPassword(), account.getPassword())) {
			throw new AccountInvalidPasswordException(passwordDto.getNewPassword());
		}

		passwordValidation.validatePassword(passwordDto.getNewPassword());

		account.setPassword(passwordValidation.encryptPassword(passwordDto.getNewPassword()));
		accountRepository.save(account);
	}
}