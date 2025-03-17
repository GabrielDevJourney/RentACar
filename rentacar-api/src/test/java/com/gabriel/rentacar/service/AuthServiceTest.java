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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private AccountService accountService;

	@Mock
	private AccountRepository accountRepository;

	@Mock
	private AccountMapper accountMapper;

	@Mock
	private PasswordValidation passwordValidation;

	@Mock
	private EmailValidation emailValidation;

	@Mock
	private JwtTokenUtil jwtTokenUtil;

	@InjectMocks
	private AuthService authService;

	private AccountDto accountDto;
	private AccountEntity activeAccountEntity;
	private AccountEntity inactiveAccountEntity;
	private AuthRequestDto authRequestDto;
	private AuthResponseDto authResponseDto;
	private ChangePasswordDto changePasswordDto;

	@BeforeEach
	void setUp() {
		// Setup AccountDto
		accountDto = new AccountDto();
		accountDto.setFirstName("John");
		accountDto.setLastName("Doe");
		accountDto.setEmail("john.doe@example.com");
		accountDto.setPassword("P@ssw0rd123");
		accountDto.setPhoneNumber("915547852");
		accountDto.setAge(25);

		// Setup active AccountEntity
		activeAccountEntity = new AccountEntity();
		activeAccountEntity.setId(1L);
		activeAccountEntity.setFirstName("John");
		activeAccountEntity.setLastName("Doe");
		activeAccountEntity.setEmail("john.doe@example.com");
		activeAccountEntity.setPassword("hashed_password");
		activeAccountEntity.setPhoneNumber("915547852");
		activeAccountEntity.setAge(25);
		activeAccountEntity.setActive(true);
		activeAccountEntity.setUserRole(UserRole.USER);

		// Setup inactive AccountEntity
		inactiveAccountEntity = new AccountEntity();
		inactiveAccountEntity.setId(2L);
		inactiveAccountEntity.setFirstName("Jane");
		inactiveAccountEntity.setLastName("Smith");
		inactiveAccountEntity.setEmail("jane.smith@example.com");
		inactiveAccountEntity.setPassword("hashed_password");
		inactiveAccountEntity.setPhoneNumber("916547852");
		inactiveAccountEntity.setAge(30);
		inactiveAccountEntity.setActive(false);
		inactiveAccountEntity.setUserRole(UserRole.USER);

		// Setup AuthRequestDto
		authRequestDto = new AuthRequestDto();
		authRequestDto.setEmail("john.doe@example.com");
		authRequestDto.setPassword("P@ssw0rd123");

		// Setup AuthResponseDto
		authResponseDto = new AuthResponseDto();
		authResponseDto.setEmail("john.doe@example.com");
		authResponseDto.setFirstName("John");
		authResponseDto.setLastName("Doe");
		authResponseDto.setToken("jwt_token");

		// Setup ChangePasswordDto
		changePasswordDto = new ChangePasswordDto();
		changePasswordDto.setCurrentPassword("OldP@ssw0rd");
		changePasswordDto.setNewPassword("NewP@ssw0rd");
	}

	// ========== REGISTER ACCOUNT TESTS ==========

	@Test
	void when_RegisteringAccount_then_Success() {
		// Setup
		doNothing().when(accountService).createAccount(any(AccountDto.class));

		// Act
		authService.registerAccount(accountDto);

		// Assert
		verify(accountService).createAccount(accountDto);
	}

	// ========== AUTHENTICATE TESTS ==========

	@Test
	void when_Authenticating_then_Success() {
		// Setup
		String normalizedEmail = "john.doe@example.com";
		when(emailValidation.validateEmailFormatAndNormalize(authRequestDto.getEmail())).thenReturn(normalizedEmail);
		when(accountRepository.findByEmail(normalizedEmail)).thenReturn(Optional.of(activeAccountEntity));
		when(passwordValidation.matches(authRequestDto.getPassword(), activeAccountEntity.getPassword())).thenReturn(true);
		when(jwtTokenUtil.generateToken(eq(normalizedEmail), anyList())).thenReturn("jwt_token");
		when(accountMapper.toAuthResponseDto(activeAccountEntity)).thenReturn(authResponseDto);

		// Act
		AuthResponseDto result = authService.authenticate(authRequestDto);

		// Assert
		assertNotNull(result);
		assertEquals("john.doe@example.com", result.getEmail());
		assertEquals("John", result.getFirstName());
		assertEquals("Doe", result.getLastName());
		assertEquals("jwt_token", result.getToken());
		verify(emailValidation).validateEmailFormatAndNormalize(authRequestDto.getEmail());
		verify(accountRepository).findByEmail(normalizedEmail);
		verify(passwordValidation).matches(authRequestDto.getPassword(), activeAccountEntity.getPassword());
		verify(jwtTokenUtil).generateToken(eq(normalizedEmail), anyList());
		verify(accountMapper).toAuthResponseDto(activeAccountEntity);
	}

	@Test
	void when_Authenticating_with_InvalidEmail_then_ThrowsException() {
		// Setup
		when(emailValidation.validateEmailFormatAndNormalize(authRequestDto.getEmail())).thenReturn("john.doe@example.com");
		when(accountRepository.findByEmail(anyString())).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(AccountInvalidAuthException.class, () -> authService.authenticate(authRequestDto));
		verify(accountRepository).findByEmail(anyString());
		verify(passwordValidation, never()).matches(anyString(), anyString());
		verify(jwtTokenUtil, never()).generateToken(anyString(), anyList());
	}

	@Test
	void when_Authenticating_with_InvalidPassword_then_ThrowsException() {
		// Setup
		String normalizedEmail = "john.doe@example.com";
		when(emailValidation.validateEmailFormatAndNormalize(authRequestDto.getEmail())).thenReturn(normalizedEmail);
		when(accountRepository.findByEmail(normalizedEmail)).thenReturn(Optional.of(activeAccountEntity));
		when(passwordValidation.matches(authRequestDto.getPassword(), activeAccountEntity.getPassword())).thenReturn(false);

		// Act & Assert
		assertThrows(AccountInvalidPasswordException.class, () -> authService.authenticate(authRequestDto));
		verify(accountRepository).findByEmail(normalizedEmail);
		verify(passwordValidation).matches(authRequestDto.getPassword(), activeAccountEntity.getPassword());
		verify(jwtTokenUtil, never()).generateToken(anyString(), anyList());
	}

	@Test
	void when_Authenticating_with_InactiveAccount_then_ThrowsException() {
		// Setup
		String normalizedEmail = "jane.smith@example.com";
		when(emailValidation.validateEmailFormatAndNormalize(authRequestDto.getEmail())).thenReturn(normalizedEmail);
		when(accountRepository.findByEmail(normalizedEmail)).thenReturn(Optional.of(inactiveAccountEntity));
		when(passwordValidation.matches(authRequestDto.getPassword(), inactiveAccountEntity.getPassword())).thenReturn(true);

		// Act & Assert
		assertThrows(AccountNotActiveException.class, () -> authService.authenticate(authRequestDto));
		verify(accountRepository).findByEmail(normalizedEmail);
		verify(passwordValidation).matches(authRequestDto.getPassword(), inactiveAccountEntity.getPassword());
		verify(jwtTokenUtil, never()).generateToken(anyString(), anyList());
	}

	// ========== CHANGE PASSWORD TESTS ==========

	@Test
	void when_ChangingPassword_then_Success() {
		// Setup
		Long accountId = 1L;
		when(accountRepository.findById(accountId)).thenReturn(Optional.of(activeAccountEntity));
		when(passwordValidation.matches(changePasswordDto.getCurrentPassword(), activeAccountEntity.getPassword())).thenReturn(true);
		when(passwordValidation.encryptPassword(changePasswordDto.getNewPassword())).thenReturn("new_hashed_password");

		// Act
		authService.changePassword(accountId, changePasswordDto);

		// Assert
		assertEquals("new_hashed_password", activeAccountEntity.getPassword());

		// Verify the correct interactions
		verify(accountRepository).findById(accountId);
		verify(passwordValidation).matches(changePasswordDto.getCurrentPassword(), "hashed_password");
		verify(passwordValidation).validatePassword(changePasswordDto.getNewPassword());
		verify(passwordValidation).encryptPassword(changePasswordDto.getNewPassword());
		verify(accountRepository).save(activeAccountEntity);
	}

	@Test
	void when_ChangingPassword_with_AccountNotFound_then_ThrowsException() {
		// Setup
		Long accountId = 3L;
		when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(AccountNotFoundException.class, () -> authService.changePassword(accountId, changePasswordDto));
		verify(accountRepository).findById(accountId);
		verify(passwordValidation, never()).matches(anyString(), anyString());
		verify(passwordValidation, never()).validatePassword(anyString());
		verify(passwordValidation, never()).encryptPassword(anyString());
		verify(accountRepository, never()).save(any());
	}

	@Test
	void when_ChangingPassword_with_InvalidCurrentPassword_then_ThrowsException() {
		// Setup
		Long accountId = 1L;
		when(accountRepository.findById(accountId)).thenReturn(Optional.of(activeAccountEntity));
		when(passwordValidation.matches(changePasswordDto.getCurrentPassword(), activeAccountEntity.getPassword())).thenReturn(false);

		// Act & Assert
		assertThrows(AccountInvalidPasswordException.class, () -> authService.changePassword(accountId, changePasswordDto));
		verify(accountRepository).findById(accountId);
		verify(passwordValidation).matches(changePasswordDto.getCurrentPassword(), activeAccountEntity.getPassword());
		verify(passwordValidation, never()).validatePassword(anyString());
		verify(passwordValidation, never()).encryptPassword(anyString());
		verify(accountRepository, never()).save(any());
	}

	@Test
	void when_ChangingPassword_with_InvalidNewPassword_then_ThrowsException() {
		// Setup
		Long accountId = 1L;
		when(accountRepository.findById(accountId)).thenReturn(Optional.of(activeAccountEntity));
		when(passwordValidation.matches(changePasswordDto.getCurrentPassword(), activeAccountEntity.getPassword())).thenReturn(true);
		doThrow(new AccountInvalidPasswordException("Password too weak")).when(passwordValidation).validatePassword(anyString());

		// Act & Assert
		assertThrows(AccountInvalidPasswordException.class, () -> authService.changePassword(accountId, changePasswordDto));
		verify(accountRepository).findById(accountId);
		verify(passwordValidation).matches(changePasswordDto.getCurrentPassword(), activeAccountEntity.getPassword());
		verify(passwordValidation).validatePassword(changePasswordDto.getNewPassword());
		verify(passwordValidation, never()).encryptPassword(anyString());
		verify(accountRepository, never()).save(any());
	}
}