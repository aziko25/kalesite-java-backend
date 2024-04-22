package kalesite.kalesite.Services.Payme;

import kalesite.kalesite.Exceptions.*;
import kalesite.kalesite.Models.Payme.Entities.*;
import kalesite.kalesite.Models.Payme.Result.*;
import kalesite.kalesite.Repositories.Payme.OrderRepository;
import kalesite.kalesite.Repositories.Payme.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MerchantService {

    private static final long TIME_EXPIRED = 43_200_000L;

    private final OrderRepository orderRepository;

    private final TransactionRepository transactionRepository;

    private CustomerOrder order;

    //@Override
    public Map<String, CheckPerformTransactionResult> checkPerformTransaction(int amount, Account account) throws OrderNotExistsException, WrongAmountException {

        order = orderRepository.findById(account.getKaleUz()).orElseThrow(() -> new OrderNotExistsException("Order not found", -31050, "order"));

        if (amount != order.getAmount()) {

            throw new WrongAmountException("Wrong amount", -31001, "amount");
        }

        CheckPerformTransactionResult checkPerformTransactionResult = new CheckPerformTransactionResult();
        checkPerformTransactionResult.setAllow(true);

        Map<String, CheckPerformTransactionResult> result = new HashMap<>();
        result.put("result", checkPerformTransactionResult);

        return result;
    }

    //@Override
    public Map<String, CreateTransactionResult> createTransaction(String id, Date time, int amount, Account account) throws UnableCompleteException, OrderNotExistsException, WrongAmountException {

        OrderTransaction transaction = transactionRepository.findByOrder(order);

        if (transaction != null && !Objects.equals(id, transaction.getPaycomId())) {

            throw new UnableCompleteException("Unable to complete operation", -31050, "transaction");
        }

        transaction = transactionRepository.findByPaycomId(id);

        if (transaction == null) {

            if (checkPerformTransaction(amount, account).get("result").isAllow()) {

                OrderTransaction newTransaction = new OrderTransaction();

                newTransaction.setPaycomId(id);
                newTransaction.setPaycomTime(time);
                newTransaction.setCreateTime(new Date());
                newTransaction.setState(TransactionState.STATE_IN_PROGRESS);
                newTransaction.setOrder(order);
                newTransaction.setPerformTime(0L);
                newTransaction.setCancelTime(0L);

                transactionRepository.save(newTransaction);

                CreateTransactionResult createTransactionResult = new CreateTransactionResult(newTransaction.getCreateTime(), newTransaction.getPaycomId(), newTransaction.getState().getCode());
                Map<String, CreateTransactionResult> result = new HashMap<>();
                result.put("result", createTransactionResult);

                return result;
            }
        }
        else {

            if (transaction.getState() == TransactionState.STATE_IN_PROGRESS) {

                if (System.currentTimeMillis() - transaction.getPaycomTime().getTime() > TIME_EXPIRED) {

                    throw new UnableCompleteException("Transaction is timed out.", -31008, "transaction");

                }
                else {

                    CreateTransactionResult createTransactionResult = new CreateTransactionResult(transaction.getCreateTime(), transaction.getPaycomId(), transaction.getState().getCode());
                    Map<String, CreateTransactionResult> result = new HashMap<>();
                    result.put("result", createTransactionResult);

                    return result;
                }
            }
            else {

                throw new UnableCompleteException("Transaction state prevents completion", -31008, "transaction");
            }
        }

        throw new UnableCompleteException("Unable to complete operation", -31008, "transaction");
    }

    //@Override
    public Map<String, PerformTransactionResult> performTransaction(String id) throws TransactionNotFoundException, UnableCompleteException {

        OrderTransaction transaction = transactionRepository.findByPaycomId(id);

        if (transaction != null) {

            if (transaction.getState() == TransactionState.STATE_IN_PROGRESS) {

                if (System.currentTimeMillis() - transaction.getPaycomTime().getTime() > TIME_EXPIRED) {

                    transaction.setState(TransactionState.STATE_CANCELED);
                    transactionRepository.save(transaction);

                    throw new UnableCompleteException("Transaction timed out and was canceled", -31008, "transaction");
                }
                else {

                    transaction.setState(TransactionState.STATE_DONE);
                    transaction.setPerformTimes(new Date());
                    transactionRepository.save(transaction);

                    PerformTransactionResult performTransactionResult = new PerformTransactionResult(transaction.getPaycomId(), transaction.getPerformTime(), transaction.getState().getCode());
                    Map<String, PerformTransactionResult> result = new HashMap<>();
                    result.put("result", performTransactionResult);

                    return result;
                }
            }
            else if (transaction.getState() == TransactionState.STATE_DONE) {

                PerformTransactionResult performTransactionResult = new PerformTransactionResult(transaction.getPaycomId(), transaction.getPerformTime(), transaction.getState().getCode());
                Map<String, PerformTransactionResult> result = new HashMap<>();
                result.put("result", performTransactionResult);

                return result;
            }
            else {

                throw new UnableCompleteException("Transaction in an invalid state for completion.", -31008, "transaction");
            }
        }
        else {

            throw new TransactionNotFoundException("Order transaction not found", -31003, "transaction");
        }
    }

    //@Override
    public Map<String, CancelTransactionResult> cancelTransaction(String id, OrderCancelReason reason) throws TransactionNotFoundException, UnableCancelTransactionException {

        OrderTransaction transaction = transactionRepository.findByPaycomId(id);

        if (transaction != null) {

            if (transaction.getState() == TransactionState.STATE_DONE) {

                if (Boolean.TRUE.equals(transaction.getOrder() != null && transaction.getOrder().isDelivered())) {

                    throw new UnableCancelTransactionException("Transaction cannot be canceled as the order has been delivered.", -31007, "transaction");
                }
                else {

                    transaction.setState(TransactionState.STATE_POST_CANCELED);
                }
            }
            else {
                transaction.setState(TransactionState.STATE_CANCELED);
            }

            transaction.setCancelTimes(new Date());
            transaction.setReason(reason);
            transactionRepository.save(transaction);

            CancelTransactionResult cancelTransactionResult = new CancelTransactionResult(transaction.getPaycomId(), transaction.getCancelTime(), transaction.getState().getCode());
            Map<String, CancelTransactionResult> result = new HashMap<>();
            result.put("result", cancelTransactionResult);

            return result;
        }
        else {

            throw new TransactionNotFoundException("Order transaction not found", -31003, "transaction");
        }
    }

    //@Override
    public Map<String, CheckTransactionResult> checkTransaction(String id) throws TransactionNotFoundException {

        OrderTransaction transaction = transactionRepository.findByPaycomId(id);

        if (transaction != null) {

            CheckTransactionResult checkTransactionResult = new CheckTransactionResult(transaction.getCreateTime(),
                    transaction.getPerformTime(),
                    transaction.getCancelTime(),
                    transaction.getPaycomId(),
                    transaction.getState().getCode(),
                    transaction.getReason() != null ? transaction.getReason().getCode() : null);

            Map<String, CheckTransactionResult> result = new HashMap<>();
            result.put("result", checkTransactionResult);

            return result;
        }
        else {

            throw new TransactionNotFoundException("Order transaction not found", -31003, "transaction");
        }
    }

    //@Override
    public Map<String, Object> getStatement(Date from, Date to) {

        List<GetStatementResult> results = new ArrayList<>();

        List<OrderTransaction> transactions = transactionRepository.findByPaycomTimeAndState(from, to, TransactionState.STATE_DONE);

        if (transactions != null) {

            results = transactions.stream()
                    .map(transaction -> new GetStatementResult(
                            transaction.getPaycomId(),
                            transaction.getPaycomTime(),
                            transaction.getOrder() != null ? transaction.getOrder().getAmount() : null,
                            new Account(transaction.getOrder() != null ? transaction.getOrder().getId() : null),
                            transaction.getCreateTime(),
                            transaction.getPerformTime(),
                            transaction.getCancelTime(),
                            transaction.getId().toString(),
                            transaction.getState().getCode(),
                            transaction.getReason() != null ? transaction.getReason().getCode() : null
                    ))
                    .collect(Collectors.toList());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("result", new Transactions(results));

        return result;
    }
}