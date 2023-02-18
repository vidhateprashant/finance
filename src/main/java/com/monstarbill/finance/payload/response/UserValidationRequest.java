package com.monstarbill.finance.payload.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Builder
@ToString
public class UserValidationRequest {
	private boolean isNewRecord;
	private String email;
	private String password;
	private List<Long> roles;
}
