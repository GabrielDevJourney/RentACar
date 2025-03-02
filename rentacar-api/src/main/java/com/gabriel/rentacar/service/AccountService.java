package com.gabriel.rentacar.service;

import com.gabriel.rentacar.dto.account.AccountDto;
import com.gabriel.rentacar.dto.account.FirstLastNameDto;
import com.gabriel.rentacar.entity.AccountEntity;
import com.gabriel.rentacar.exception.accountException.*;
import com.gabriel.rentacar.mapper.AccountMapper;
import com.gabriel.rentacar.repository.AccountRepository;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {
	private final AccountRepository accountRepository;
	private final AccountMapper accountMapper;

	public AccountService(AccountRepository accountRepository, AccountMapper accountMapper) {
		this.accountRepository = accountRepository;
		this.accountMapper = accountMapper;
	}

	//REST ENDPOINTS
	public void createAccount(AccountDto accountDto) {

		validateNotNull(accountDto, "account");

		validateName(accountDto.getFirstName(), "first name");
		validateName(accountDto.getLastName(), "last name");

		String accountEmail = validateEmailFormatAndNormalize(accountDto.getEmail());
		validateEmailUniqueness(accountEmail);

		Integer age = validateAge(accountDto.getAge(), accountEmail);

		String phoneNumber = validatePhoneNumberFormat(accountDto.getPhoneNumber());
		validatePhoneNumberUniqueness(phoneNumber);

		accountDto.setEmail(accountEmail);
		accountDto.setAge(age);
		accountDto.setPhoneNumber(phoneNumber);

		save(accountDto);
	}

	public void activateAccount(Long id) {
		AccountEntity account = getAccountEntityById(id);

		if (account.isActive()) {
			throw new AccountAlreadyActiveException(id);
		}

		account.setActive(true);
		accountRepository.save(account);
	}

	public void deactivateAccount(Long id) {
		AccountEntity account = getAccountEntityById(id);

		if (!account.isActive()) {
			throw new AccountAlreadyDeactivatedException(id);
		}

		account.setActive(false);
		accountRepository.save(account);
	}

	public void deleteAccount(Long id) {
		AccountEntity account = getAccountEntityById(id);
		accountRepository.delete(account);
	}

	public void updateFirstNameAndLastName(Long id, FirstLastNameDto firstLastNameDto) {
		validateNotNull(firstLastNameDto, "nameData");
		AccountEntity accountEntity = getAccountEntityById(id);

		validateName(firstLastNameDto.getFirstName(), "first name");
		accountEntity.setFirstName(firstLastNameDto.getFirstName());

		validateName(firstLastNameDto.getLastName(), "last name");
		accountEntity.setLastName(firstLastNameDto.getLastName());


		accountRepository.save(accountEntity);
	}

	public void updateFullAccountDetails(Long id, AccountDto accountDto) {
		validateNotNull(accountDto, "account");
		AccountEntity accountEntity = getAccountEntityById(id);

		validateName(accountDto.getFirstName(), "first name");
		validateName(accountDto.getLastName(), "last name");

		String accountEmail = validateEmailFormatAndNormalize(accountDto.getEmail());

		//email exists but belongs to a different account
		if (!accountEmail.equalsIgnoreCase(accountEntity.getEmail()) && existsByEmail(accountEmail)) {
			throw new AccountEmailAlreadyExistsException(accountEmail);
		}

		Integer age = validateAge(accountDto.getAge(), accountEmail);

		String phoneNumber = validatePhoneNumberFormat(accountDto.getPhoneNumber());
		//phone exists but belongs to a different account
		if (!phoneNumber.equals(accountEntity.getPhoneNumber()) && existsByPhoneNumber(phoneNumber)) {
			throw new AccountPhoneNumberAlreadyExistsException(phoneNumber);
		}

		// Update entity
		accountEntity.setFirstName(accountDto.getFirstName());
		accountEntity.setLastName(accountDto.getLastName());
		accountEntity.setEmail(accountEmail);
		accountEntity.setAge(age);
		accountEntity.setPhoneNumber(phoneNumber);

		accountRepository.save(accountEntity);
	}

	public void updateAccountAge(Long id, Integer age) {
		AccountEntity accountEntity = getAccountEntityById(id);
		validateAge(age, accountEntity.getEmail());
		accountEntity.setAge(age);
		accountRepository.save(accountEntity);
	}

	public void updateAccountEmail(Long id, String email) {
		AccountEntity accountEntity = getAccountEntityById(id);
		String normalizedEmail = validateEmailFormatAndNormalize(email);

		//email exists but belongs to a different account
		if (normalizedEmail.equalsIgnoreCase(accountEntity.getEmail()) && existsByEmail(normalizedEmail)) {
			throw new AccountEmailAlreadyExistsException(normalizedEmail);
		}

		accountEntity.setEmail(normalizedEmail);
		accountRepository.save(accountEntity);
	}

	public void updateAccountPhoneNumber(Long id, String phoneNumber) {
		AccountEntity accountEntity = getAccountEntityById(id);
		String validatedPhone = validatePhoneNumberFormat(phoneNumber);

		//phone exists but belongs to a different account
		if (validatedPhone.equals(accountEntity.getPhoneNumber()) && existsByPhoneNumber(validatedPhone)) {
			throw new AccountPhoneNumberAlreadyExistsException(validatedPhone);
		}

		accountEntity.setPhoneNumber(validatedPhone);
		accountRepository.save(accountEntity);
	}

	public AccountDto getAccountDtoById(Long id) {
		return accountMapper.toDto(getAccountEntityById(id));
	}

	public AccountEntity getAccountEntityById(Long id) {
		validateNotNull(id, "id");
		return accountRepository.findById(id)
				.orElseThrow(() -> new AccountNotFoundException(id));
	}

	public List<AccountDto> getDeactivatedAccounts() {
		return accountMapper.toDtoList(accountRepository.findByActiveIsFalse());
	}

	public List<FirstLastNameDto> getFirstNameAndLastNameAccountsThatAreDeactivated() {
		List<AccountEntity> deactivatedAccounts = accountRepository.findByActiveIsFalseOrderByFirstNameAscLastNameAsc();
		return deactivatedAccounts.stream()
				.map(accountMapper::toFirstLastNameDto)
				.toList();
	}

	public List<AccountDto> getAllAccounts() {
		List<AccountEntity> accounts = accountRepository.findAll();
		return accountMapper.toDtoList(accounts);
	}

	// repo helper methods
	private void save(AccountDto accountDto) {
		accountRepository.save(accountMapper.toEntity(accountDto));
	}

	private boolean existsByEmail(String email) {
		return accountRepository.existsByEmailIgnoreCase(email);
	}

	private boolean existsByPhoneNumber(String phoneNumber) {
		return accountRepository.existsByPhoneNumber(phoneNumber);
	}

	// Validation helper methods
	private void validateNotNull(Object obj, String fieldName) {
		if (obj == null) {
			throw new AccountInvalidDataException(fieldName, fieldName + " cannot be null");
		}
	}

	private void validateName(String name, String fieldName) {
		if (name == null || name.trim().isEmpty()) {
			throw new AccountInvalidNameFormat(fieldName, "Name cannot be empty");
		}

		String trimmedName = name.trim();

		// Length check
		if (trimmedName.length() < 2) {
			throw new AccountInvalidNameFormat(fieldName,
					String.format("%s is too short (minimum 2 characters)", fieldName));
		}

		if (trimmedName.length() > 50) {
			throw new AccountInvalidNameFormat(fieldName,
					String.format("%s is too long (maximum 50 characters)", fieldName));
		}

		// Character check
		if (!trimmedName.matches("^[a-zA-Z\\s'-]+$")) {
			throw new AccountInvalidNameFormat(fieldName,
					String.format("%s can only contain letters, spaces, hyphens and apostrophes", fieldName));
		}
	}

	private String validateEmailFormatAndNormalize(String email) {
		if (email == null || email.trim().isEmpty()) {
			throw new AccountInvalidDataException("email", "Email cannot be empty");
		}

		String normalizedEmail = email.trim().toLowerCase();
		EmailValidator emailValidator = EmailValidator.getInstance();

		if (!emailValidator.isValid(normalizedEmail)) {
			throw new AccountInvalidEmailFormatException(normalizedEmail);
		}

		return normalizedEmail;
	}

	private void validateEmailUniqueness(String email) {
		if (existsByEmail(email)) {
			throw new AccountEmailAlreadyExistsException(email);
		}
	}

	private Integer validateAge(Integer age, String email) {
		if (age == null) {
			throw new AccountInvalidDataException("age", "Age cannot be null");
		}

		if (age < 18 || age > 99) {
			throw new AccountInvalidAgeException(email);
		}

		return age;
	}

	private String validatePhoneNumberFormat(String phoneNumber) {
		if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
			throw new AccountInvalidDataException("phoneNumber", "Phone number cannot be empty");
		}

		if (!phoneNumber.matches("^(91|92|93|96)\\d{7}$")) {
			throw new AccountInvalidNumberException(phoneNumber);
		}

		return phoneNumber;
	}

	private void validatePhoneNumberUniqueness(String phoneNumber) {
		if (existsByPhoneNumber(phoneNumber)) {
			throw new AccountPhoneNumberAlreadyExistsException(phoneNumber);
		}
	}
}