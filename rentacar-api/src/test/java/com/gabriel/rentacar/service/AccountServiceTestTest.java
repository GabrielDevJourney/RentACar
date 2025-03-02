package com.gabriel.rentacar.service;

import com.gabriel.rentacar.dto.account.AccountDto;
import com.gabriel.rentacar.dto.account.FirstLastNameDto;
import com.gabriel.rentacar.entity.AccountEntity;
import com.gabriel.rentacar.exception.accountException.*;
import com.gabriel.rentacar.mapper.AccountMapper;
import com.gabriel.rentacar.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTestTest {

	@Mock
	AccountRepository accountRepository;

	@Mock
	AccountMapper accountMapper;

	@InjectMocks
	AccountService accountService;

	Long accountId = 1L;

	AccountDto accountDtoTest = new AccountDto("gabriel", "pereira", "gabi@gmail.com", "915547852", 20);
	AccountEntity accountEntityTest = new AccountEntity(1L, "gabriel", "pereira", false, "gabi@gmail.com", "915547852", 20);
	AccountEntity accountEntityTestActiveTrue = new AccountEntity(1L, "gabriel", "pereira", true, "gabi@gmail.com",
			"915547852", 20);


	private void mockAccountLookup(AccountEntity account) {
		when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));
	}



	// Tests for createAccount
	@Test
	void when_CreatingAccount_then_Success() {
		//Setup
		String email = accountDtoTest.getEmail();

		when(accountRepository.existsByEmail(email)).thenReturn(false);
		when(accountMapper.toEntity(accountDtoTest)).thenReturn(accountEntityTest);

		//Act
		accountService.createAccount(accountDtoTest);

		//Assert
		verify(accountRepository).existsByEmail(email);
		verify(accountMapper).toEntity(accountDtoTest);
		verify(accountRepository).save(accountEntityTest);

	}

	@Test
	void when_CreatingAccount_with_ExistingEmail_then_ThrowsException() {
		//Setup
		String existsEmail = accountDtoTest.getEmail();
		when(accountRepository.existsByEmail(existsEmail)).thenReturn(true);

		//Act
		assertThrows(AccountEmailAlreadyExistsException.class, () -> {
			accountService.createAccount(accountDtoTest);
		});

		//Assert
		verify(accountRepository, times(1)).existsByEmail(existsEmail);
		verify(accountMapper, never()).toEntity(any());
		verify(accountRepository, never()).save(any());
	}

	@ParameterizedTest
	@ValueSource(strings = {"", " ", "a", "x "})
	void when_CreatingAccount_with_InvalidFirstName_then_ThrowsException(String invalidFirstName) {
		// Setup
		AccountDto invalidFirstNameDto = accountDtoTest;
		invalidFirstNameDto.setFirstName(invalidFirstName);

		// Act & Assert
		assertThrows(
				AccountInvalidNameFormat.class,
				() -> accountService.createAccount(invalidFirstNameDto)
		);
	}

	// Last Name Validation
	@ParameterizedTest
	@ValueSource(strings = {"", " ", "b", "y "})
	void when_CreatingAccount_with_InvalidLastName_then_ThrowsException(String invalidLastName) {
		// Setup
		AccountDto invalidLastNameDto = accountDtoTest;
		invalidLastNameDto.setLastName(invalidLastName);

		// Act & Assert
		assertThrows(
				AccountInvalidNameFormat.class,
				() -> accountService.createAccount(invalidLastNameDto)
		);
	}

	// Email Validation
	@ParameterizedTest
	@ValueSource(strings = {
			"invalid.email",           // Missing @ symbol
			"@missingusername.com",    // No username before @
			"username@",               // No domain after @
			"username@domain",          // No top-level domain
			"user name@domain.com",    // Space in username
			"username@domain..com",    // Double dots in domain
			"username@-domain.com",    // Invalid domain start
			"username@domain.com-",    // Invalid domain end
			"username@domain.c",       // Too short top-level domain
			"username@domain.toolongdomainextension"  // Too long top-level domain
	})
	void when_CreatingAccount_with_InvalidEmailFormat_then_ThrowsException(String invalidEmail) {
		// Setup
		AccountDto invalidEmailFormatDto = accountDtoTest;
		invalidEmailFormatDto.setEmail(invalidEmail);

		// Act & Assert
		assertThrows(
				AccountInvalidEmailFormatException.class,
				() -> accountService.createAccount(invalidEmailFormatDto)
		);
	}

	// Phone Number Validation
	@ParameterizedTest
	@ValueSource(strings = {
			"123",
			"12345",
			"abc123456",
			"91234",
			"912345678901"
	})
	void when_CreatingAccount_with_InvalidPhoneNumber_then_ThrowsException(String invalidPhoneNumber) {
		// Setup
		AccountDto invalidPhoneNumberDto = accountDtoTest;
		invalidPhoneNumberDto.setPhoneNumber(invalidPhoneNumber);
		when(accountRepository.existsByEmail(invalidPhoneNumberDto.getEmail())).thenReturn(false);

		// Act & Assert
		assertThrows(
				AccountInvalidNumberException.class,
				() -> accountService.createAccount(invalidPhoneNumberDto)
		);
	}

	// Age Validation
	@ParameterizedTest
	@ValueSource(ints = {17, 100, 101, 0, -1})
	void when_CreatingAccount_with_InvalidAge_then_ThrowsException(int invalidAge) {
		// Setup
		AccountDto invalidAgeDto = accountDtoTest;
		invalidAgeDto.setAge(invalidAge);
		when(accountRepository.existsByEmail(invalidAgeDto.getEmail())).thenReturn(false);

		// Act & Assert
		assertThrows(
				AccountInvalidAgeException.class,
				() -> accountService.createAccount(invalidAgeDto)
		);
	}


	// Tests for activateAccount
	@Test
	void when_ActivatingAccount_then_Success(){
		//Setup
		when(accountRepository.findById(accountId)).thenReturn(Optional.of(accountEntityTest));

		//Act
		accountService.activateAccount(accountId);

		//Assert
		assertTrue(accountEntityTest.isActive());
		verify(accountRepository).save(accountEntityTest);
	}

	@Test
	void when_ActivatingAccount_with_NotFoundAccount_then_ThrowsException(){
		//Setup
		Long idToFailTest = 2L;
		when(accountRepository.findById(idToFailTest)).thenReturn(Optional.empty());

		//Assert within act
		assertThrows(AccountNotFoundException.class, () -> {
			accountService.activateAccount(idToFailTest);
		});
		verify(accountRepository,never()).save(any());
	}

	@Test
	void when_ActivatingAccount_with_AlreadyActiveAccount_then_ThrowsException(){
		//Setup

		mockAccountLookup(accountEntityTestActiveTrue);

		//Assert within act
		assertThrows(AccountAlreadyActiveException.class, () -> {
			accountService.activateAccount(accountId);
		});

		verify(accountRepository,never()).save(any());
	}

	// Tests for deactivateAccount
	@Test
	void when_DeactivatingAccount_then_Success(){
		//Setup

		mockAccountLookup(accountEntityTestActiveTrue);

		//Act
		accountService.deactivateAccount(accountId);

		//Assert
		assertFalse(accountEntityTestActiveTrue.isActive());
		verify(accountRepository).save(accountEntityTestActiveTrue);
	}

	@Test
	void when_DeactivatingAccount_with_NotFoundAccount_then_ThrowsException(){
		//Setup
		Long idToFailTest = 2L;

		when(accountRepository.findById(idToFailTest)).thenReturn(Optional.empty());

		//Assert within act
		assertThrows(AccountNotFoundException.class, () -> {
			accountService.deactivateAccount(idToFailTest);
		});

		verify(accountRepository,never()).save(any());
	}

	@Test
	void when_DeactivatingAccount_with_AlreadyDeactivatedAccount_then_ThrowsException(){
		when(accountRepository.findById(accountId)).thenReturn(Optional.of(accountEntityTest));

		//Assert within act
		assertThrows(AccountAlreadyDeactivatedException.class, () -> {
			accountService.deactivateAccount(accountId);
		});

		verify(accountRepository,never()).save(any());
	}

	// Tests for deleteAccount
	@Test
	void when_DeletingAccount_then_Success(){
		when(accountRepository.findById(accountId)).thenReturn(Optional.of(accountEntityTest));

		//Act
		accountService.deleteAccount(accountId);

		//Assert
		verify(accountRepository).delete(accountEntityTest);
	}

	@Test
	void when_DeletingAccount_with_AccountNotFound_then_ThrowsException(){
		Long idToFailTest = 1L;

		when(accountRepository.findById(idToFailTest)).thenReturn(Optional.empty());

		assertThrows(AccountNotFoundException.class, () -> {
			accountService.deleteAccount(idToFailTest);
		});

		verify(accountRepository, never()).delete(any());
	}

	// Tests for updateFirstNameAndLastName
	@Test
	void when_UpdatingFirstNameAndLastName_then_Success() {
		// Setup
		FirstLastNameDto firstLastNameDtoUpdate = new FirstLastNameDto("NewFirst", "NewLast");

		mockAccountLookup(accountEntityTestActiveTrue);

		// Act
		accountService.updateFirstNameAndLastName(accountId, firstLastNameDtoUpdate);

		// Assert
		assertEquals("NewFirst", accountEntityTestActiveTrue.getFirstName());
		assertEquals("NewLast", accountEntityTestActiveTrue.getLastName());
		verify(accountRepository).save(accountEntityTestActiveTrue);
	}

	@Test
	void when_UpdatingFirstNameAndLastName_with_NotFoundAccount_then_ThrowsException() {
		// Setup
		FirstLastNameDto firstLastNameDtoUpdate = new FirstLastNameDto("NewFirst", "NewLast");

		when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(AccountNotFoundException.class, () -> {
			accountService.updateFirstNameAndLastName(accountId, firstLastNameDtoUpdate);
		});

		verify(accountRepository, never()).save(any());
	}

	// Tests for updateFullAccountDetails
	@Test
	void when_UpdatingFullAccountDetails_then_Success() {
		// Setup
		AccountEntity accountEntityTest = new AccountEntity(
				accountId, "OldFirst", "OldLast", true, "old@example.com", "915547852", 25);
		AccountDto fullUpdateDto = new AccountDto(
				"NewFirst", "NewLast", "new@example.com", "916547852", 30);

		when(accountRepository.findById(accountId)).thenReturn(Optional.of(accountEntityTest));

		// Act
		accountService.updateFullAccountDetails(accountId, fullUpdateDto);

		// Assert
		//todo maybe create fields for easier testing
		assertEquals("NewFirst", accountEntityTest.getFirstName());
		assertEquals("NewLast", accountEntityTest.getLastName());
		assertEquals("new@example.com", accountEntityTest.getEmail());
		verify(accountRepository).save(accountEntityTest);
	}

	@Test
	void when_UpdatingFullAccountDetails_with_NotFoundAccount_then_ThrowsException() {
		// Setup
		AccountDto fullUpdateDto = new AccountDto(
				"NewFirst", "NewLast", "new@example.com", "916547852", 30);

		when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

		// Act and Assert
		assertThrows(AccountNotFoundException.class, () -> {
			accountService.updateFullAccountDetails(accountId, fullUpdateDto);
		});

		verify(accountRepository, never()).save(any());
	}

	// Tests for updateAccountAge
	@Test
	void when_UpdatingAccountAge_then_Success() {
		// Setup
		Integer newAge = 35;

		mockAccountLookup(accountEntityTestActiveTrue);

		// Act
		accountService.updateAccountAge(accountId, newAge);

		// Assert
		assertEquals(newAge, accountEntityTestActiveTrue.getAge());
		verify(accountRepository).save(accountEntityTestActiveTrue);
	}

	@Test
	void when_UpdatingAccountAge_with_NotFoundAccount_then_ThrowsException() {
		// Setup
		Integer newAge = 35;

		when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(AccountNotFoundException.class, () -> {
			accountService.updateAccountAge(accountId, newAge);
		});

		verify(accountRepository, never()).save(any());
	}

	@Test
	void when_UpdatingAccountAge_with_InvalidAge_then_ThrowsException() {
		// Setup
		Integer invalidAge = 15; // Below minimum

		mockAccountLookup(accountEntityTestActiveTrue);

		// Act & Assert
		assertThrows(AccountInvalidAgeException.class, () -> {
			accountService.updateAccountAge(accountId, invalidAge);
		});

		verify(accountRepository, never()).save(any());
	}

	// Tests for updateAccountEmail
	@Test
	void when_UpdatingAccountEmail_then_Success() {
		// Setup
		String newEmail = "new@example.com";

		mockAccountLookup(accountEntityTestActiveTrue);

		// Act
		accountService.updateAccountEmail(accountId, newEmail);

		// Assert
		assertEquals(newEmail, accountEntityTestActiveTrue.getEmail());
		verify(accountRepository).save(accountEntityTestActiveTrue);
	}

	@Test
	void when_UpdatingAccountEmail_with_NotFoundAccount_then_ThrowsException() {
		// Setup
		String newEmail = "new@example.com";

		when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(AccountNotFoundException.class, () -> {
			accountService.updateAccountEmail(accountId, newEmail);
		});

		verify(accountRepository, never()).save(any());
	}

	@Test
	void when_UpdatingAccountEmail_with_ExistingEmail_then_ThrowsException() {
		// Setup
		String newEmail = accountEntityTestActiveTrue.getEmail();

		mockAccountLookup(accountEntityTestActiveTrue);
		when(accountRepository.existsByEmail(newEmail)).thenReturn(true);

		// Act & Assert
		assertThrows(AccountEmailAlreadyExistsException.class, () -> {
			accountService.updateAccountEmail(accountId, newEmail);
		});

		verify(accountRepository).findById(accountId);
		verify(accountRepository).existsByEmail(newEmail);
		verify(accountRepository, never()).save(any());
	}

	// Tests for updateAccountPhoneNumber
	@Test
	void when_UpdatingAccountPhoneNumber_then_Success() {
		//Setup
		String newPhoneNumber = "965547852";

		mockAccountLookup(accountEntityTestActiveTrue);

		// Act
		accountService.updateAccountPhoneNumber(accountId, newPhoneNumber);

		// Assert
		assertEquals(newPhoneNumber, accountEntityTestActiveTrue.getPhoneNumber());
		verify(accountRepository).save(accountEntityTestActiveTrue);
	}

	@Test
	void when_UpdatingAccountPhoneNumber_with_NotFoundAccount_then_ThrowsException() {
		// Setup
		String newPhoneNumber = "965547852";

		when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(AccountNotFoundException.class, () -> {
			accountService.updateAccountPhoneNumber(accountId, newPhoneNumber);
		});

		verify(accountRepository, never()).save(any());
	}

	@Test
	void when_UpdatingAccountPhoneNumber_with_InvalidNumber_then_ThrowsException() {
		// Setup
		String invalidPhoneNumber = "12345678"; // Invalid format

		mockAccountLookup(accountEntityTestActiveTrue);

		// Act & Assert
		assertThrows(AccountInvalidNumberException.class, () -> {
			accountService.updateAccountPhoneNumber(accountId, invalidPhoneNumber);
		});

		verify(accountRepository, never()).save(any());
	}

}