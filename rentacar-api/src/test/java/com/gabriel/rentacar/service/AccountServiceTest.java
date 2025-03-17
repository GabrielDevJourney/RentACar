package com.gabriel.rentacar.service;

import com.gabriel.rentacar.dto.account.AccountConfirmationDto;
import com.gabriel.rentacar.dto.account.AccountDto;
import com.gabriel.rentacar.dto.account.FirstLastNameDto;
import com.gabriel.rentacar.entity.AccountEntity;
import com.gabriel.rentacar.enums.UserRole;
import com.gabriel.rentacar.exception.accountException.*;
import com.gabriel.rentacar.mapper.AccountMapper;
import com.gabriel.rentacar.repository.AccountRepository;
import com.gabriel.rentacar.utils.PasswordValidation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

	@Mock
	private AccountRepository accountRepository;

	@Mock
	private AccountMapper accountMapper;

	@Mock
	private PasswordValidation passwordValidator;

	@InjectMocks
	private AccountService accountService;

	private Long accountId = 1L;
	private AccountDto accountDto;
	private AccountEntity inactiveAccountEntity;
	private AccountEntity activeAccountEntity;
	private FirstLastNameDto firstLastNameDto;
	private AccountConfirmationDto confirmationDto;

	@BeforeEach
	void setUp() {
		// Setup AccountDto
		accountDto = new AccountDto();
		accountDto.setFirstName("Gabriel");
		accountDto.setLastName("Pereira");
		accountDto.setEmail("gabi@gmail.com");
		accountDto.setPassword("P@ssw0rd123");
		accountDto.setPhoneNumber("915547852");
		accountDto.setAge(20);

		// Setup inactive AccountEntity
		inactiveAccountEntity = new AccountEntity();
		inactiveAccountEntity.setId(accountId);
		inactiveAccountEntity.setFirstName("Gabriel");
		inactiveAccountEntity.setLastName("Pereira");
		inactiveAccountEntity.setActive(false);
		inactiveAccountEntity.setEmail("gabi@gmail.com");
		inactiveAccountEntity.setPassword("encrypted_password");
		inactiveAccountEntity.setPhoneNumber("915547852");
		inactiveAccountEntity.setAge(20);
		inactiveAccountEntity.setUserRole(UserRole.USER);

		// Setup active AccountEntity
		activeAccountEntity = new AccountEntity();
		activeAccountEntity.setId(accountId);
		activeAccountEntity.setFirstName("Gabriel");
		activeAccountEntity.setLastName("Pereira");
		activeAccountEntity.setActive(true);
		activeAccountEntity.setEmail("gabi@gmail.com");
		activeAccountEntity.setPassword("encrypted_password");
		activeAccountEntity.setPhoneNumber("915547852");
		activeAccountEntity.setAge(20);
		activeAccountEntity.setUserRole(UserRole.USER);

		// Setup FirstLastNameDto
		firstLastNameDto = new FirstLastNameDto();
		firstLastNameDto.setFirstName("NewFirst");
		firstLastNameDto.setLastName("NewLast");

		// Setup AccountConfirmationDto
		confirmationDto = new AccountConfirmationDto();
		confirmationDto.setEmail("gabi@gmail.com");
		confirmationDto.setPassword("P@ssw0rd123");
	}

	private void mockAccountLookup(AccountEntity account) {
		when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));
	}

	// Tests for createAccount
	@Test
	void when_CreatingAccount_then_Success() {
		// Setup
		String email = accountDto.getEmail();
		when(accountRepository.existsByEmail(email)).thenReturn(false);
		when(accountRepository.existsByPhoneNumber(accountDto.getPhoneNumber())).thenReturn(false);
		when(accountMapper.toEntity(accountDto)).thenReturn(inactiveAccountEntity);
		when(passwordValidator.encryptPassword(accountDto.getPassword())).thenReturn("encrypted_password");

		// Act
		accountService.createAccount(accountDto);

		// Assert
		verify(accountRepository).existsByEmail(email);
		verify(accountMapper).toEntity(accountDto);
		verify(accountRepository).save(inactiveAccountEntity);
	}

	@Test
	void when_CreatingAccount_with_ExistingEmail_then_ThrowsException() {
		// Setup
		String existsEmail = accountDto.getEmail();
		when(accountRepository.existsByEmail(existsEmail)).thenReturn(true);

		// Act & Assert
		assertThrows(AccountEmailAlreadyExistsException.class, () -> {
			accountService.createAccount(accountDto);
		});

		// Verify
		verify(accountRepository, times(1)).existsByEmail(existsEmail);
		verify(accountMapper, never()).toEntity(any());
		verify(accountRepository, never()).save(any());
	}

	@ParameterizedTest
	@ValueSource(strings = {"", " ", "a", "x "})
	void when_CreatingAccount_with_InvalidFirstName_then_ThrowsException(String invalidFirstName) {
		// Setup
		accountDto.setFirstName(invalidFirstName);

		// Act & Assert
		assertThrows(
				AccountInvalidNameFormat.class,
				() -> accountService.createAccount(accountDto)
		);
	}

	// Last Name Validation
	@ParameterizedTest
	@ValueSource(strings = {"", " ", "b", "y "})
	void when_CreatingAccount_with_InvalidLastName_then_ThrowsException(String invalidLastName) {
		// Setup
		accountDto.setLastName(invalidLastName);

		// Act & Assert
		assertThrows(
				AccountInvalidNameFormat.class,
				() -> accountService.createAccount(accountDto)
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
		accountDto.setEmail(invalidEmail);

		// Act & Assert
		assertThrows(
				AccountInvalidEmailFormatException.class,
				() -> accountService.createAccount(accountDto)
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
		accountDto.setPhoneNumber(invalidPhoneNumber);
		when(accountRepository.existsByEmail(accountDto.getEmail())).thenReturn(false);

		// Act & Assert
		assertThrows(
				AccountInvalidNumberException.class,
				() -> accountService.createAccount(accountDto)
		);
	}

	// Age Validation
	@ParameterizedTest
	@ValueSource(ints = {17, 100, 101, 0, -1})
	void when_CreatingAccount_with_InvalidAge_then_ThrowsException(int invalidAge) {
		// Setup
		accountDto.setAge(invalidAge);
		when(accountRepository.existsByEmail(accountDto.getEmail())).thenReturn(false);

		// Act & Assert
		assertThrows(
				AccountInvalidAgeException.class,
				() -> accountService.createAccount(accountDto)
		);
	}

	// Tests for confirmAccount
	@Test
	void when_ConfirmingAccount_then_Success() {
		// Setup
		when(accountRepository.findByEmail(confirmationDto.getEmail())).thenReturn(Optional.of(inactiveAccountEntity));
		when(passwordValidator.matches(confirmationDto.getPassword(), inactiveAccountEntity.getPassword())).thenReturn(true);

		// Act
		accountService.confirmAccount(confirmationDto.getEmail(), confirmationDto.getPassword());

		// Assert
		assertTrue(inactiveAccountEntity.isActive());
		verify(accountRepository).save(inactiveAccountEntity);
	}

	@Test
	void when_ConfirmingAccount_with_AlreadyActiveAccount_then_ThrowsException() {
		// Setup
		when(accountRepository.findByEmail(confirmationDto.getEmail())).thenReturn(Optional.of(activeAccountEntity));
		when(passwordValidator.matches(confirmationDto.getPassword(), activeAccountEntity.getPassword())).thenReturn(true);

		// Act & Assert
		assertThrows(AccountAlreadyActiveException.class, () -> {
			accountService.confirmAccount(confirmationDto.getEmail(), confirmationDto.getPassword());
		});

		verify(accountRepository, never()).save(any());
	}

	@Test
	void when_ConfirmingAccount_with_InvalidPassword_then_ThrowsException() {
		// Setup
		when(accountRepository.findByEmail(confirmationDto.getEmail())).thenReturn(Optional.of(inactiveAccountEntity));
		when(passwordValidator.matches(confirmationDto.getPassword(), inactiveAccountEntity.getPassword())).thenReturn(false);

		// Act & Assert
		assertThrows(AccountInvalidPasswordException.class, () -> {
			accountService.confirmAccount(confirmationDto.getEmail(), confirmationDto.getPassword());
		});

		verify(accountRepository, never()).save(any());
	}

	// Tests for deactivateAccount
	@Test
	void when_DeactivatingAccount_then_Success() {
		// Setup
		mockAccountLookup(activeAccountEntity);

		// Act
		accountService.deactivateAccount(accountId);

		// Assert
		assertFalse(activeAccountEntity.isActive());
		verify(accountRepository).save(activeAccountEntity);
	}

	@Test
	void when_DeactivatingAccount_with_NotFoundAccount_then_ThrowsException() {
		// Setup
		Long idToFailTest = 2L;
		when(accountRepository.findById(idToFailTest)).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(AccountNotFoundException.class, () -> {
			accountService.deactivateAccount(idToFailTest);
		});

		verify(accountRepository, never()).save(any());
	}

	@Test
	void when_DeactivatingAccount_with_AlreadyDeactivatedAccount_then_ThrowsException() {
		// Setup
		mockAccountLookup(inactiveAccountEntity);

		// Act & Assert
		assertThrows(AccountAlreadyDeactivatedException.class, () -> {
			accountService.deactivateAccount(accountId);
		});

		verify(accountRepository, never()).save(any());
	}

	// Tests for deleteAccount
	@Test
	void when_DeletingAccount_then_Success() {
		// Setup
		mockAccountLookup(inactiveAccountEntity);

		// Act
		accountService.deleteAccount(accountId);

		// Assert
		verify(accountRepository).delete(inactiveAccountEntity);
	}

	@Test
	void when_DeletingAccount_with_AccountNotFound_then_ThrowsException() {
		// Setup
		Long idToFailTest = 1L;
		when(accountRepository.findById(idToFailTest)).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(AccountNotFoundException.class, () -> {
			accountService.deleteAccount(idToFailTest);
		});

		verify(accountRepository, never()).delete(any());
	}

	// Tests for updateFirstNameAndLastName
	@Test
	void when_UpdatingFirstNameAndLastName_then_Success() {
		// Setup
		mockAccountLookup(activeAccountEntity);

		// Act
		accountService.updateFirstNameAndLastName(accountId, firstLastNameDto);

		// Assert
		assertEquals("NewFirst", activeAccountEntity.getFirstName());
		assertEquals("NewLast", activeAccountEntity.getLastName());
		verify(accountRepository).save(activeAccountEntity);
	}

	@Test
	void when_UpdatingFirstNameAndLastName_with_NotFoundAccount_then_ThrowsException() {
		// Setup
		when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(AccountNotFoundException.class, () -> {
			accountService.updateFirstNameAndLastName(accountId, firstLastNameDto);
		});

		verify(accountRepository, never()).save(any());
	}

	// Tests for updateFullAccountDetails
	@Test
	void when_UpdatingFullAccountDetails_then_Success() {
		// Setup
		AccountEntity testAccountEntity = new AccountEntity();
		testAccountEntity.setId(accountId);
		testAccountEntity.setFirstName("OldFirst");
		testAccountEntity.setLastName("OldLast");
		testAccountEntity.setActive(true);
		testAccountEntity.setEmail("old@example.com");
		testAccountEntity.setPassword("encrypted_password");
		testAccountEntity.setPhoneNumber("915547852");
		testAccountEntity.setAge(25);

		AccountDto updateDto = new AccountDto();
		updateDto.setFirstName("NewFirst");
		updateDto.setLastName("NewLast");
		updateDto.setEmail("new@example.com");
		updateDto.setPassword("NewP@ssw0rd123");
		updateDto.setPhoneNumber("916547852");
		updateDto.setAge(30);

		when(accountRepository.findById(accountId)).thenReturn(Optional.of(testAccountEntity));

		// Act
		accountService.updateFullAccountDetails(accountId, updateDto);

		// Assert
		assertEquals("NewFirst", testAccountEntity.getFirstName());
		assertEquals("NewLast", testAccountEntity.getLastName());
		assertEquals("new@example.com", testAccountEntity.getEmail());
		verify(accountRepository).save(testAccountEntity);
	}

	@Test
	void when_UpdatingFullAccountDetails_with_NotFoundAccount_then_ThrowsException() {
		// Setup
		when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

		// Act and Assert
		assertThrows(AccountNotFoundException.class, () -> {
			accountService.updateFullAccountDetails(accountId, accountDto);
		});

		verify(accountRepository, never()).save(any());
	}

	// Tests for updateAccountAge
	@Test
	void when_UpdatingAccountAge_then_Success() {
		// Setup
		Integer newAge = 35;
		mockAccountLookup(activeAccountEntity);

		// Act
		accountService.updateAccountAge(accountId, newAge);

		// Assert
		assertEquals(newAge, activeAccountEntity.getAge());
		verify(accountRepository).save(activeAccountEntity);
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
		mockAccountLookup(activeAccountEntity);

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
		mockAccountLookup(activeAccountEntity);

		// Act
		accountService.updateAccountEmail(accountId, newEmail);

		// Assert
		assertEquals(newEmail, activeAccountEntity.getEmail());
		verify(accountRepository).save(activeAccountEntity);
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
		String newEmail = activeAccountEntity.getEmail();
		mockAccountLookup(activeAccountEntity);
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
		// Setup
		String newPhoneNumber = "965547852";
		mockAccountLookup(activeAccountEntity);

		// Act
		accountService.updateAccountPhoneNumber(accountId, newPhoneNumber);

		// Assert
		assertEquals(newPhoneNumber, activeAccountEntity.getPhoneNumber());
		verify(accountRepository).save(activeAccountEntity);
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
		mockAccountLookup(activeAccountEntity);

		// Act & Assert
		assertThrows(AccountInvalidNumberException.class, () -> {
			accountService.updateAccountPhoneNumber(accountId, invalidPhoneNumber);
		});

		verify(accountRepository, never()).save(any());
	}

	// Tests for getDeactivatedAccounts
	@Test
	void when_GettingDeactivatedAccounts_then_Success() {
		// Setup
		List<AccountEntity> deactivatedAccounts = Arrays.asList(inactiveAccountEntity);
		List<AccountDto> expectedDtos = Arrays.asList(accountDto);

		when(accountRepository.findByActiveIsFalse()).thenReturn(deactivatedAccounts);
		when(accountMapper.toDtoList(deactivatedAccounts)).thenReturn(expectedDtos);

		// Act
		List<AccountDto> result = accountService.getDeactivatedAccounts();

		// Assert
		assertEquals(expectedDtos, result);
		verify(accountRepository).findByActiveIsFalse();
		verify(accountMapper).toDtoList(deactivatedAccounts);
	}

	// Tests for getFirstNameAndLastNameAccountsThatAreDeactivated
	@Test
	void when_GettingFirstNameAndLastNameOfDeactivatedAccounts_then_Success() {
		// Setup
		List<AccountEntity> deactivatedAccounts = Arrays.asList(inactiveAccountEntity);

		when(accountRepository.findByActiveIsFalseOrderByFirstNameAscLastNameAsc()).thenReturn(deactivatedAccounts);
		when(accountMapper.toFirstLastNameDto(inactiveAccountEntity)).thenReturn(firstLastNameDto);

		// Act
		List<FirstLastNameDto> result = accountService.getFirstNameAndLastNameAccountsThatAreDeactivated();

		// Assert
		assertEquals(1, result.size());
		assertEquals(firstLastNameDto, result.get(0));
		verify(accountRepository).findByActiveIsFalseOrderByFirstNameAscLastNameAsc();
	}

	// Tests for getAllAccounts
	@Test
	void when_GettingAllAccounts_then_Success() {
		// Setup
		List<AccountEntity> allAccounts = Arrays.asList(activeAccountEntity, inactiveAccountEntity);
		List<AccountDto> expectedDtos = Arrays.asList(accountDto, accountDto);

		when(accountRepository.findAll()).thenReturn(allAccounts);
		when(accountMapper.toDtoList(allAccounts)).thenReturn(expectedDtos);

		// Act
		List<AccountDto> result = accountService.getAllAccounts();

		// Assert
		assertEquals(expectedDtos, result);
		verify(accountRepository).findAll();
		verify(accountMapper).toDtoList(allAccounts);
	}
}