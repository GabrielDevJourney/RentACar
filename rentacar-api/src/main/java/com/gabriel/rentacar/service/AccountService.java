package com.gabriel.rentacar.service;

import com.gabriel.rentacar.dto.account.AccountDto;
import com.gabriel.rentacar.dto.account.FirstLastNameDto;
import com.gabriel.rentacar.entity.AccountEntity;
import com.gabriel.rentacar.exception.accountException.*;
import com.gabriel.rentacar.mapper.AccountMapper;
import com.gabriel.rentacar.repository.AccountRepository;
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
	//todo filter name to only allow letters
	public void createAccount(AccountDto accountDto) {
		String accountEmail = accountDto.getEmail();
		if (existsByEmail(accountEmail)) {
			throw new AccountEmailAlreadyExistsException(accountEmail);
		}

		Integer age = accountDto.getAge();
		if (age < 18 || age > 99) {
			throw new AccountInvalidAgeException(accountDto.getEmail());
		}

		String phoneNumber = accountDto.getPhoneNumber();
		if (phoneNumber != null && !phoneNumber.matches("^(91|92|93|96)\\d{7}$")) {
			throw new AccountInvalidNumberException(phoneNumber);
		}

		save(accountDto);
	}

	public void activateAccount(Long id) {
		AccountEntity account = accountRepository.findById(id)
				.orElseThrow(() -> new AccountNotFoundException(id));

		if (account.isActive()) {
			throw new AccountAlreadyActiveException(id);
		}

		account.setActive(true);
		accountRepository.save(account);
	}

	public void deactivateAccount(Long id) {
		AccountEntity account = accountRepository.findById(id)
				.orElseThrow(() -> new AccountNotFoundException(id));

		if (!account.isActive()) {
			throw new AccountAlreadyDeactivatedException(id);
		}

		account.setActive(false);
		accountRepository.save(account);
	}

	public void deleteAccount(Long id) {
		AccountEntity account = accountRepository.findById(id)
				.orElseThrow(() -> new AccountNotFoundException(id));
		accountRepository.delete(account);
	}

	public void updateFirstNameAndLastName(Long id, FirstLastNameDto firstLastNameDto) {
		AccountEntity accountEntity = accountRepository.findById(id)
				.orElseThrow(() -> new AccountNotFoundException(id));

		if(firstLastNameDto.getFirstName() != null && !firstLastNameDto.getFirstName().isEmpty()){
			accountEntity.setFirstName(firstLastNameDto.getFirstName());
		}
		if(firstLastNameDto.getLastName() != null && !firstLastNameDto.getLastName().isEmpty()){
			accountEntity.setLastName(firstLastNameDto.getLastName());
		}

		accountRepository.save(accountEntity);
	}

	public void updateFullAccountDetails(Long id, AccountDto accountDto) {
		AccountEntity accountEntity = accountRepository.findById(id)
				.orElseThrow(() -> new AccountNotFoundException(id));

		accountEntity.setFirstName(accountDto.getFirstName());
		accountEntity.setLastName(accountDto.getLastName());
		accountEntity.setEmail(accountDto.getEmail());

		accountRepository.save(accountEntity);

	}

	public void updateAccountAge(Long id, Integer age){
		AccountEntity accountEntity = accountRepository.findById(id)
				.orElseThrow(() -> new AccountNotFoundException(id));
		if(age < 18 || age > 99){
			throw new AccountInvalidAgeException(id);
		}
		accountEntity.setAge(age);
		accountRepository.save(accountEntity);
	}

	public void updateAccountEmail(Long id, String email) {
		AccountEntity accountEntity = accountRepository.findById(id)
				.orElseThrow(() -> new AccountNotFoundException(id));

		if (accountRepository.existsByEmail(email)) {
			throw new AccountEmailAlreadyExistsException(email);
		}

		accountEntity.setEmail(email);
		accountRepository.save(accountEntity);
	}
	public void updateAccountPhoneNumber(Long id, String phoneNumber) {
		AccountEntity accountEntity = accountRepository.findById(id)
				.orElseThrow(() -> new AccountNotFoundException(id));

		if (!phoneNumber.matches("^(91|92|93|96)\\d{7}$")) {
			throw new AccountInvalidNumberException(phoneNumber);
		}
		accountEntity.setPhoneNumber(phoneNumber);
		accountRepository.save(accountEntity);
	}


	public AccountDto getAccountById(Long id) {
		AccountEntity accountEntity = accountRepository.findById(id)
				.orElseThrow(() -> new AccountNotFoundException(id));
		return accountMapper.toDto(accountEntity);
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

	//H
	private void save(AccountDto accountDto) {
		accountRepository.save(accountMapper.toEntity(accountDto));
	}

	private boolean existsByEmail(String email) {
		return accountRepository.existsByEmail(email);
	}

}