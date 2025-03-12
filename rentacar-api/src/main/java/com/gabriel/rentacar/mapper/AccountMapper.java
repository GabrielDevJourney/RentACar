package com.gabriel.rentacar.mapper;

import com.gabriel.rentacar.dto.account.AccountDto;
import com.gabriel.rentacar.dto.account.FirstLastNameDto;
import com.gabriel.rentacar.dto.auth.AuthResponseDto;
import com.gabriel.rentacar.entity.AccountEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AccountMapper {
	AccountDto toDto(AccountEntity entity);
	AccountEntity toEntity(AccountDto dto);
	List<AccountDto> toDtoList(List<AccountEntity> entities);

	@Named("toFirstLastNameDto")
	@Mapping(target = "firstName", source = "accountEntity.firstName")
	@Mapping(target = "lastName", source = "accountEntity.lastName")
	FirstLastNameDto toFirstLastNameDto(AccountEntity accountEntity);

	@Named("toAuthResponseDto")
	@Mapping(target = "email", source = "accountEntity.email")
	@Mapping(target = "firstName", source = "accountEntity.firstName")
	@Mapping(target = "lastName", source = "accountEntity.lastName")
	AuthResponseDto toAuthResponseDto(AccountEntity accountEntity);
}