package com.gabriel.rentacar.service;


import com.gabriel.rentacar.dto.auth.AuthRequestDto;
import com.gabriel.rentacar.dto.auth.AuthResponseDto;
import com.gabriel.rentacar.dto.auth.ChangePasswordDto;
import com.gabriel.rentacar.entity.AccountEntity;
import com.gabriel.rentacar.exception.accountException.AccountInvalidAuthException;
import com.gabriel.rentacar.exception.accountException.AccountInvalidPasswordException;
import com.gabriel.rentacar.exception.accountException.AccountNotActiveException;
import com.gabriel.rentacar.exception.accountException.AccountNotFoundException;
import com.gabriel.rentacar.mapper.AccountMapper;
import com.gabriel.rentacar.repository.AccountRepository;
import com.gabriel.rentacar.utils.EmailValidation;
import com.gabriel.rentacar.utils.PasswordValidation;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
	private final AccountRepository accountRepository;
	private final AccountMapper accountMapper;
	private final PasswordValidation passwordValidation;
	private final EmailValidation emailValidation;

	public AuthService(AccountRepository accountRepository, AccountMapper accountMapper,
	                   PasswordValidation passwordValidation, EmailValidation emailValidation) {
		this.accountRepository = accountRepository;
		this.accountMapper = accountMapper;
		this.passwordValidation = passwordValidation;
		this.emailValidation = emailValidation;
	}


	public AuthResponseDto authenticate(AuthRequestDto authRequest) {
		String email = emailValidation.validateEmailFormatAndNormalize(authRequest.getEmail());

		AccountEntity account = accountRepository.findByEmail(email)
				.orElseThrow(AccountInvalidAuthException::new);

		if (!passwordValidation.matches(authRequest.getPassword(), account.getPassword())) {
			throw new AccountInvalidPasswordException("Invalid password");
		}

		if (!account.isActive()) {
			throw new AccountNotActiveException(account.getId());
		}

		// Return authenticated user info
		return accountMapper.toAuthResponseDto(account);
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