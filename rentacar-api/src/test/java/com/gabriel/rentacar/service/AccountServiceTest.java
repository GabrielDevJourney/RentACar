package com.gabriel.rentacar.service;

import com.gabriel.rentacar.dto.account.AccountDto;
import com.gabriel.rentacar.dto.account.FirstLastNameDto;
import com.gabriel.rentacar.entity.AccountEntity;
import com.gabriel.rentacar.exception.accountException.*;
import com.gabriel.rentacar.mapper.AccountMapper;
import com.gabriel.rentacar.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

	@Mock
	AccountRepository accountRepository;

	@Mock
	AccountMapper accountMapper;

	@InjectMocks
	AccountService accountService;

	Long accountId = 1L;

	// Tests for createAccount
	@Test
	void when_CreatingAccount_then_Success() {
		//Setup
		AccountDto accountDtoTest = new AccountDto("gabriel", "pereira", "gabi@gmail.com", "915547852", 20);
		AccountEntity accountEntityTest = new AccountEntity(1L, "gabriel", "pereira", false, "gabi@gmail.com", "915547852", 20);
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
		AccountDto existsEmailDto = new AccountDto("gabriel", "pereira", "gabi@gmail.com", "945547852", 20);
		String existsEmail = existsEmailDto.getEmail();
		when(accountRepository.existsByEmail(existsEmail)).thenReturn(true);

		//Act
		assertThrows(AccountEmailAlreadyExistsException.class, () -> {
			accountService.createAccount(existsEmailDto);
		});

		//Assert
		verify(accountRepository, times(1)).existsByEmail(existsEmail);
		verify(accountMapper, never()).toEntity(any());
		verify(accountRepository, never()).save(any());
	}

	@Test
	void when_CreatingAccount_with_InvalidNumber_then_ThrowsException() {
		//Setup
		AccountDto invalidPhoneDto = new AccountDto(
				"Test", "User", "test@example.com", "945557852", 25
		);
		when(accountRepository.existsByEmail(invalidPhoneDto.getEmail())).thenReturn(false);

		//Act
		assertThrows(AccountInvalidNumberException.class, () -> {
			accountService.createAccount(invalidPhoneDto);
		});

		//Assert
		verify(accountRepository).existsByEmail(anyString());
		verify(accountRepository, never()).save(any());
	}

	@Test
	void when_CreatingAccount_with_InvalidAge_then_ThrowsException() {
		//Setup
		AccountDto invalidAgeDto = new AccountDto(
				"Test", "User", "test@example.com", "912345678", 15 //below 18
		);
		when(accountRepository.existsByEmail(invalidAgeDto.getEmail())).thenReturn(false);

		//Act
		assertThrows(AccountInvalidAgeException.class, () -> {
			accountService.createAccount(invalidAgeDto);
		});

		//Assert
		verify(accountRepository).existsByEmail(anyString());
		verify(accountRepository, never()).save(any());
	}

	// Tests for activateAccount
	@Test
	void when_ActivatingAccount_then_Success(){
		//Setup
		AccountEntity accountEntityTest = new AccountEntity(accountId,"test","demo",false,"testdemo@gmail.com",
				"915547852",20);
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
		AccountEntity accountEntityTest = new AccountEntity(1L,"test","demo",false,"testdemo@gmail.com",
				"915547852",20);
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
		Long accountId = 2L;
		AccountEntity accountEntityTest = new AccountEntity(accountId,"test","demo",true,"testdemo@gmail.com",
				"915547852",20);

		when(accountRepository.findById(accountId)).thenReturn(Optional.of(accountEntityTest));

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
		AccountEntity accountEntityTest = new AccountEntity(accountId,"test","demo",true,"testdemo@gmail.com",
				"915547852",20);

		when(accountRepository.findById(accountId)).thenReturn(Optional.of(accountEntityTest));

		//Act
		accountService.deactivateAccount(accountId);

		//Assert
		assertFalse(accountEntityTest.isActive());
		verify(accountRepository).save(accountEntityTest);
	}

	@Test
	void when_DeactivatingAccount_with_NotFoundAccount_then_ThrowsException(){
		//Setup
		Long idToFailTest = 2L;
		AccountEntity accountEntityTest = new AccountEntity(1L,"test","demo",true,"testdemo@gmail.com",
				"915547852",20);

		when(accountRepository.findById(idToFailTest)).thenReturn(Optional.empty());

		//Assert within act
		assertThrows(AccountNotFoundException.class, () -> {
			accountService.deactivateAccount(idToFailTest);
		});

		verify(accountRepository,never()).save(any());
	}

	@Test
	void when_DeactivatingAccount_with_AlreadyDeactivatedAccount_then_ThrowsException(){
		//Setup
		Long accountId = 2L;
		AccountEntity accountEntityTest = new AccountEntity(accountId,"test","demo",false,"testdemo@gmail.com",
				"915547852",20);

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
		//Setup
		AccountEntity accountEntityTest = new AccountEntity(accountId,"test","demo",true,"testdemo@gmail.com",
				"915547852",20);

		when(accountRepository.findById(accountId)).thenReturn(Optional.of(accountEntityTest));

		//Act
		accountService.deleteAccount(accountId);

		//Assert
		verify(accountRepository).delete(accountEntityTest);
	}

	@Test
	void when_DeletingAccount_with_AccountNotFound_then_ThrowsException(){
		Long idToFailTest = 1L;
		AccountEntity accountEntityTest = new AccountEntity(2L,"test","demo",true,"testdemo@gmail.com",
				"915547852",20);

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
		AccountEntity accountEntityTest = new AccountEntity(
				accountId, "OldFirst", "OldLast", true, "test@example.com", "915547852", 25);
		FirstLastNameDto firstLastNameDtoUpdate = new FirstLastNameDto("NewFirst", "NewLast");

		when(accountRepository.findById(accountId)).thenReturn(Optional.of(accountEntityTest));

		// Act
		accountService.updateFirstNameAndLastName(accountId, firstLastNameDtoUpdate);

		// Assert
		assertEquals("NewFirst", accountEntityTest.getFirstName());
		assertEquals("NewLast", accountEntityTest.getLastName());
		verify(accountRepository).save(accountEntityTest);
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
		AccountEntity accountEntityTest = new AccountEntity(
				accountId, "Test", "User", true, "test@example.com", "915547852", 25);
		Integer newAge = 35;

		when(accountRepository.findById(accountId)).thenReturn(Optional.of(accountEntityTest));

		// Act
		accountService.updateAccountAge(accountId, newAge);

		// Assert
		assertEquals(newAge, accountEntityTest.getAge());
		verify(accountRepository).save(accountEntityTest);
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
		AccountEntity accountEntityTest = new AccountEntity(
				accountId, "Test", "User", true, "test@example.com", "915547852", 25);
		Integer invalidAge = 15; // Below minimum

		when(accountRepository.findById(accountId)).thenReturn(Optional.of(accountEntityTest));

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
		AccountEntity accountEntityTest = new AccountEntity(
				accountId, "Test", "User", true, "old@example.com", "915547852", 25);
		String newEmail = "new@example.com";

		when(accountRepository.findById(accountId)).thenReturn(Optional.of(accountEntityTest));
		when(accountRepository.existsByEmail(newEmail)).thenReturn(false);

		// Act
		accountService.updateAccountEmail(accountId, newEmail);

		// Assert
		assertEquals(newEmail, accountEntityTest.getEmail());
		verify(accountRepository).save(accountEntityTest);
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
		AccountEntity accountEntityTest = new AccountEntity(
				accountId, "Test", "User", true, "old@example.com", "915547852", 25);
		String existingEmail = "existing@example.com";

		when(accountRepository.findById(accountId)).thenReturn(Optional.of(accountEntityTest));
		when(accountRepository.existsByEmail(existingEmail)).thenReturn(true);

		// Act & Assert
		assertThrows(AccountEmailAlreadyExistsException.class, () -> {
			accountService.updateAccountEmail(accountId, existingEmail);
		});

		verify(accountRepository, never()).save(any());
	}

	// Tests for updateAccountPhoneNumber
	@Test
	void when_UpdatingAccountPhoneNumber_then_Success() {
		// Setup
		AccountEntity accountEntityTest = new AccountEntity(
				accountId, "Test", "User", true, "test@example.com", "915547852", 25);
		String newPhoneNumber = "965547852";

		when(accountRepository.findById(accountId)).thenReturn(Optional.of(accountEntityTest));

		// Act
		accountService.updateAccountPhoneNumber(accountId, newPhoneNumber);

		// Assert
		assertEquals(newPhoneNumber, accountEntityTest.getPhoneNumber());
		verify(accountRepository).save(accountEntityTest);
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
		AccountEntity accountEntityTest = new AccountEntity(
				accountId, "Test", "User", true, "test@example.com", "915547852", 25);
		String invalidPhoneNumber = "12345678"; // Invalid format

		when(accountRepository.findById(accountId)).thenReturn(Optional.of(accountEntityTest));

		// Act & Assert
		assertThrows(AccountInvalidNumberException.class, () -> {
			accountService.updateAccountPhoneNumber(accountId, invalidPhoneNumber);
		});

		verify(accountRepository, never()).save(any());
	}

}