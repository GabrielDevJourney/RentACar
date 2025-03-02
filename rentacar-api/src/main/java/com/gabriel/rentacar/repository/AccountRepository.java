package com.gabriel.rentacar.repository;

import com.gabriel.rentacar.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity,Long> {
	boolean existsByEmail(String email);
	boolean existsByPhoneNumber(String phoneNumber);

	List<AccountEntity> findByActiveIsFalse();

	List<AccountEntity> findByActiveIsFalseOrderByFirstNameAscLastNameAsc();
}
