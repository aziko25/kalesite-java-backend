package kalesite.kalesite.Services.Payme;

import kalesite.kalesite.Exceptions.*;
import kalesite.kalesite.Models.Payme.Entities.Account;
import kalesite.kalesite.Models.Payme.Entities.OrderCancelReason;
import kalesite.kalesite.Models.Payme.Entities.Transactions;
import kalesite.kalesite.Models.Payme.Result.*;

import java.util.Date;
import java.util.Map;

public interface IMerchantService {

    Map<String, CheckPerformTransactionResult> checkPerformTransaction(int amount, String id) throws WrongAmountException, OrderNotExistsException;

    Map<String, CreateTransactionResult> createTransaction(String id, Date time, int amount) throws OrderNotExistsException, WrongAmountException, UnableCompleteException;

    Map<String, PerformTransactionResult> performTransaction(String id) throws TransactionNotFoundException, UnableCompleteException;

    Map<String, CancelTransactionResult> cancelTransaction(String id, OrderCancelReason reason) throws TransactionNotFoundException, UnableCancelTransactionException;

    Map<String, CheckTransactionResult> checkTransaction(String id) throws TransactionNotFoundException;

    Map<String, Object> getStatement(Date from, Date to);
}