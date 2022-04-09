package com.db.awmd.challenge.util;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NotificationMessageUtility {
	
	public static final String INSUFFICIENT_BALANCE = "Dear Customer, you do not have sufficient amount to transfer.";

	public static String getTransferDescription(String accountNumber, BigDecimal amount, String action, BigDecimal balance) {
		String currentDate = new SimpleDateFormat("dd-MMM-yy").format(new Date());
		return new StringBuilder("Dear Customer, Account XX")
				.append(accountNumber.substring(accountNumber.length() - 3, accountNumber.length())).append(" is ")
				.append(action).append(" with INR ").append(amount).append(" on ").append(currentDate)
				.append(". The Available Balance is INR ").append(balance).append(". Call 18009999 for dispute.")
				.toString();
	}

}
