package com.db.awmd.challenge.domain;

import java.math.BigDecimal;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferBalanceRequest {
	@NotNull
	@NotEmpty
	private String fromAccountId;

	@NotNull
	@NotEmpty
	private String toAccountId;

	@NotNull
	@Min(value = 0, message = "Initial balance must be positive.")
	private BigDecimal amount;

}