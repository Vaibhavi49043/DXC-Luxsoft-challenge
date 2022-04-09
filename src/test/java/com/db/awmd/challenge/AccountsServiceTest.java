package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.fail;

import java.math.BigDecimal;

import org.junit.After;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.TransferBalanceRequest;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.InsufficientBalanceException;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.NotificationService;
import com.db.awmd.challenge.util.NotificationMessageUtility;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountsServiceTest {

	@Rule
	public ExpectedException exceptionRule = ExpectedException.none();
	@MockBean
	private NotificationService notificationService;

  @Autowired
  @InjectMocks
  private AccountsService accountsService;

  @After
  public void cleanup() {
	  this.accountsService.clearAccounts();
  }
  
  @Test
  public void addAccount() throws Exception {
    Account account = new Account("Id-123");
    account.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account);

    assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
  }

  @Test
  public void addAccount_failsOnDuplicateId() throws Exception {
    String uniqueId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueId);
    this.accountsService.createAccount(account);

    try {
      this.accountsService.createAccount(account);
      fail("Should have failed when adding duplicate account");
    } catch (DuplicateAccountIdException ex) {
      assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
    }

  }

  @Test
  public void transferMoney() throws Exception {
    Account fromAccount = new Account("Id-123");
    fromAccount.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(fromAccount);
    
    Account toAccount = new Account("Id-456");
    toAccount.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(toAccount);
    
    TransferBalanceRequest transferBalanceRequest = new TransferBalanceRequest(fromAccount.getAccountId(), toAccount.getAccountId(), new BigDecimal(500));
    this.accountsService.transferMoney(transferBalanceRequest);

    assertThat(this.accountsService.getAccount("Id-123").getBalance()).isEqualTo(new BigDecimal(500));
    assertThat(this.accountsService.getAccount("Id-456").getBalance()).isEqualTo(new BigDecimal(1500));
    
	Mockito.verify(notificationService, Mockito.times(1)).notifyAboutTransfer(fromAccount, NotificationMessageUtility
			.getTransferDescription(fromAccount.getAccountId(), new BigDecimal(500), "debited", new BigDecimal(500)));
	
	Mockito.verify(notificationService, Mockito.times(1)).notifyAboutTransfer(toAccount, NotificationMessageUtility
			.getTransferDescription(toAccount.getAccountId(), new BigDecimal(500), "credited", new BigDecimal(1500)));
  }
  
  @Test
  public void transferMoneyWithInsufficientAmount() throws Exception {
    Account fromAccount = new Account("Id-123");
    fromAccount.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(fromAccount);
    
    Account toAccount = new Account("Id-456");
    toAccount.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(toAccount);
    
    TransferBalanceRequest transferBalanceRequest = new TransferBalanceRequest(fromAccount.getAccountId(), toAccount.getAccountId(), new BigDecimal(1000));
    exceptionRule.expect(InsufficientBalanceException.class);
    exceptionRule.expectMessage(NotificationMessageUtility.INSUFFICIENT_BALANCE);
    this.accountsService.transferMoney(transferBalanceRequest);

    assertThat(this.accountsService.getAccount("Id-123").getBalance()).isEqualTo(new BigDecimal(1000));
    assertThat(this.accountsService.getAccount("Id-456").getBalance()).isEqualTo(new BigDecimal(1000));
    Mockito.verify(notificationService, Mockito.times(0)).notifyAboutTransfer(Mockito.any(), Mockito.anyString());
  }
}
