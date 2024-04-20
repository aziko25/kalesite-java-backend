/*package kalesite.kalesite.Services.Payme;

import com.googlecode.jsonrpc4j.JsonRpcError;
import com.googlecode.jsonrpc4j.JsonRpcErrors;
import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.googlecode.jsonrpc4j.JsonRpcService;
import kalesite.kalesite.Exceptions.*;
import kalesite.kalesite.Models.Payme.Entities.Account;
import kalesite.kalesite.Models.Payme.Entities.OrderCancelReason;
import kalesite.kalesite.Models.Payme.Entities.Transactions;
import kalesite.kalesite.Models.Payme.Result.*;

import java.util.Date;

@JsonRpcService("/api")
public interface IMerchantService {

    @JsonRpcErrors({
            @JsonRpcError(exception = WrongAmountException.class, code = -31001, message = "Wrong amount", data = "amount"),
            @JsonRpcError(exception = OrderNotExistsException.class, code = -31050, message = "Order not found", data = "order")
    })
    CheckPerformTransactionResult CheckPerformTransaction(
            @JsonRpcParam(value = "amount") int amount,
            @JsonRpcParam(value = "account") Account account);

    @JsonRpcErrors({
            @JsonRpcError(exception = UnableCompleteException.class, code = -31008, message = "Unable to complete operation", data = "transaction")
    })
    CreateTransactionResult CreateTransaction(
            @JsonRpcParam(value = "id") String id,
            @JsonRpcParam(value = "time") Date time,
            @JsonRpcParam(value = "amount") int amount,
            @JsonRpcParam(value = "account") Account account);

    @JsonRpcErrors({
            @JsonRpcError(exception = UnableCompleteException.class, code = -31008, message = "Unable to complete operation", data = "transaction"),
            @JsonRpcError(exception = TransactionNotFoundException.class, code = -31003, message = "Order transaction not found", data = "transaction")
    })
    PerformTransactionResult PerformTransaction(
            @JsonRpcParam(value = "id") String id);

    @JsonRpcErrors({
            @JsonRpcError(exception = UnableCancelTransactionException.class, code = -31007, message = "Unable to cancel transaction", data = "transaction"),
            @JsonRpcError(exception = TransactionNotFoundException.class, code = -31003, message = "Order transaction not found", data = "transaction")
    })
    CancelTransactionResult CancelTransaction(
            @JsonRpcParam(value = "id") String id,
            @JsonRpcParam(value = "reason") OrderCancelReason reason);

    @JsonRpcErrors({
            @JsonRpcError(exception = TransactionNotFoundException.class, code = -31003, message = "Order transaction not found", data = "transaction")
    })
    CheckTransactionResult CheckTransaction(
            @JsonRpcParam(value = "id") String id);

    Transactions GetStatement(
            @JsonRpcParam(value = "from") Date from,
            @JsonRpcParam(value = "to") Date to);

    CheckPerformTransactionResult checkPerformTransaction(int amount, Account account) throws WrongAmountException, OrderNotExistsException;

    CreateTransactionResult createTransaction(String id, Date time, int amount, Account account) throws OrderNotExistsException, WrongAmountException, UnableCompleteException;

    PerformTransactionResult performTransaction(String id) throws TransactionNotFoundException, UnableCompleteException;

    CancelTransactionResult cancelTransaction(String id, OrderCancelReason reason) throws TransactionNotFoundException, UnableCancelTransactionException;

    CheckTransactionResult checkTransaction(String id) throws TransactionNotFoundException;

    Transactions getStatement(Date from, Date to);
}*/