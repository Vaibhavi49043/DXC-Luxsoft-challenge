package com.db.awmd.challenge.service;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.TransferBalanceRequest;
import com.db.awmd.challenge.exception.AccountNotFoundException;
import com.db.awmd.challenge.exception.InsufficientBalanceException;
import com.db.awmd.challenge.repository.AccountsRepository;
import com.db.awmd.challenge.util.NotificationMessageUtility;

import lombok.Getter;

@Service
public class AccountsService {

  @Getter
  private final AccountsRepository accountsRepository;
  @Getter
  private final NotificationService notificationService;

  @Autowired
  public AccountsService(AccountsRepository accountsRepository,Optional<NotificationService> notificationService) {
    this.accountsRepository = accountsRepository;
	this.notificationService = notificationService.orElse(null);
  }

  public void createAccount(Account account) {
    this.accountsRepository.createAccount(account);
  }

  public Account getAccount(String accountId) {
    return this.accountsRepository.getAccount(accountId);
  }

  public void clearAccounts() {
	  this.accountsRepository.clearAccounts();
  }

  public void transferMoney(TransferBalanceRequest transferBalanceRequest) {
	  String fromAccountId = transferBalanceRequest.getFromAccountId();
	  String toAccountId = transferBalanceRequest.getToAccountId();
	  BigDecimal amount = transferBalanceRequest.getAmount();
	  Account fromAccount = accountsRepository.getAccount(fromAccountId);
	  Account toAccount = accountsRepository.getAccount(toAccountId);

	  validateAccounts(fromAccountId, toAccountId, fromAccount, toAccount);

	  if (fromAccount.getBalance().compareTo(BigDecimal.ONE) == 1 && fromAccount.getBalance().compareTo(amount) == 1) {
		  synchronized (Account.class) {

			  BigDecimal debitedBalance = fromAccount.getBalance().subtract(amount);

			  fromAccount.setBalance(debitedBalance);
			  accountsRepository.updateAccount(fromAccount);

			  BigDecimal creditedBalance = toAccount.getBalance().add(amount);
			  toAccount.setBalance(creditedBalance);
			  accountsRepository.updateAccount(toAccount);

			  if(null != notificationService) {
				  String transferDescription = NotificationMessageUtility.getTransferDescription(fromAccountId, amount, "debited", debitedBalance);
				  notificationService.notifyAboutTransfer(fromAccount, transferDescription);

				  transferDescription = NotificationMessageUtility.getTransferDescription(toAccountId, amount, "credited", creditedBalance);
				  notificationService.notifyAboutTransfer(toAccount, transferDescription);
			  }
		  }
	  }
	  else {
			throw new InsufficientBalanceException(NotificationMessageUtility.INSUFFICIENT_BALANCE);
	  }
  }

	private void validateAccounts(String fromAccountId, String toAccountId, Account fromAccount, Account toAccount) {
		if (null == fromAccount) {
			throw new AccountNotFoundException(new StringBuilder("Dear Customer, Account ").append(fromAccountId)
					.append(" does not exists.").toString());
		}
		if (null == toAccount) {
			throw new AccountNotFoundException(new StringBuilder("Dear Customer, Account ").append(toAccountId)
					.append(" does not exists.").toString());
		}
	}
}
