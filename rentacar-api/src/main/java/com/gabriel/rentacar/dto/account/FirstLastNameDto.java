package com.gabriel.rentacar.dto.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FirstLastNameDto {
	private String firstName;
	private String lastName;
}
