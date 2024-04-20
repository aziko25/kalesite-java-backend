package kalesite.kalesite.Services.Payme;

import kalesite.kalesite.Exceptions.*;
import kalesite.kalesite.Models.Payme.Entities.*;
import kalesite.kalesite.Models.Payme.Result.*;
import kalesite.kalesite.Repositories.Payme.OrderRepository;
import kalesite.kalesite.Repositories.Payme.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MerchantService implements IMerchantService {

    private static final long TIME_EXPIRED = 43_200_000L;

    private final OrderRepository orderRepository;

    private final TransactionRepository transactionRepository;

    private CustomerOrder order;

    @Override
    public CheckPerformTransactionResult checkPerformTransaction(int amount, String id) throws WrongAmountException, OrderNotExistsException {

        System.out.println("CPT here #1");
        order = orderRepository.findByStringId(id).orElse(null);
        System.out.println("CPT here #2");

        if (order == null) {
            throw new OrderNotExistsException();
        }
        if (amount != order.getAmount()) {
            throw new WrongAmountException();
        }

        return new CheckPerformTransactionResult(true);
    }

    @Override
    public CreateTransactionResult createTransaction(String id, Date time, int amount) throws OrderNotExistsException, WrongAmountException, UnableCompleteException {

        System.out.println("CT here #1");
        OrderTransaction transaction = transactionRepository.findByPaycomId(id);
        System.out.println("CT here #2");

        if (transaction == null) {

            if (checkPerformTransaction(amount, id).isAllow()) {

                OrderTransaction newTransaction = new OrderTransaction();

                newTransaction.setPaycomId(id);
                newTransaction.setPaycomTime(time);
                newTransaction.setCreateTime(new Date());
                newTransaction.setState(TransactionState.STATE_IN_PROGRESS);
                newTransaction.setOrder(order);

                transactionRepository.save(newTransaction);

                return new CreateTransactionResult(newTransaction.getCreateTime(), newTransaction.getId(), newTransaction.getState().getCode());
            }
        }
        else {

            if (transaction.getState() == TransactionState.STATE_IN_PROGRESS) {

                if (System.currentTimeMillis() - transaction.getPaycomTime().getTime() > TIME_EXPIRED) {

                    throw new UnableCompleteException("Transaction is timed out.");
                }
                else {

                    return new CreateTransactionResult(transaction.getCreateTime(), transaction.getId(), transaction.getState().getCode());
                }
            }
            else {

                throw new UnableCompleteException("Transaction state prevents completion.");
            }
        }

        throw new UnableCompleteException("Unable to complete transaction.");
    }

    @Override
    public PerformTransactionResult performTransaction(String id) throws TransactionNotFoundException, UnableCompleteException {

        System.out.println("PT here #1");
        OrderTransaction transaction = transactionRepository.findByPaycomId(id);
        System.out.println("PT here #2");

        if (transaction != null) {

            if (transaction.getState() == TransactionState.STATE_IN_PROGRESS) {

                if (System.currentTimeMillis() - transaction.getPaycomTime().getTime() > TIME_EXPIRED) {

                    transaction.setState(TransactionState.STATE_CANCELED);
                    transactionRepository.save(transaction);

                    throw new UnableCompleteException("Transaction timed out and was canceled.");
                }
                else {

                    transaction.setState(TransactionState.STATE_DONE);
                    transaction.setPerformTime(new Date());
                    transactionRepository.save(transaction);

                    return new PerformTransactionResult(transaction.getId(), transaction.getPerformTime(), transaction.getState().getCode());
                }
            }
            else if (transaction.getState() == TransactionState.STATE_DONE) {

                return new PerformTransactionResult(transaction.getId(), transaction.getPerformTime(), transaction.getState().getCode());
            }
            else {

                throw new UnableCompleteException("Transaction in an invalid state for completion.");
            }
        }
        else {

            throw new TransactionNotFoundException("Transaction not found.");
        }
    }

    @Override
    public CancelTransactionResult cancelTransaction(String id, OrderCancelReason reason) throws TransactionNotFoundException, UnableCancelTransactionException {

        System.out.println("CT here #1");
        OrderTransaction transaction = transactionRepository.findByPaycomId(id);
        System.out.println("CT here #2");

        if (transaction != null) {

            if (transaction.getState() == TransactionState.STATE_DONE) {

                if (Boolean.TRUE.equals(transaction.getOrder() != null && transaction.getOrder().isDelivered())) {

                    throw new UnableCancelTransactionException("Transaction cannot be canceled as the order has been delivered.");
                }
                else {

                    transaction.setState(TransactionState.STATE_POST_CANCELED);
                }
            }
            else {
                transaction.setState(TransactionState.STATE_CANCELED);
            }

            transaction.setCancelTime(new Date());
            transaction.setReason(reason);
            transactionRepository.save(transaction);

            return new CancelTransactionResult(transaction.getId(), transaction.getCancelTime(), transaction.getState().getCode());
        }
        else {

            throw new TransactionNotFoundException("Transaction not found.");
        }
    }

    @Override
    public CheckTransactionResult checkTransaction(String id) throws TransactionNotFoundException {

        System.out.println("CT here #1");
        OrderTransaction transaction = transactionRepository.findByPaycomId(id);
        System.out.println("CT here #2");

        if (transaction != null) {

            return new CheckTransactionResult(
                    transaction.getCreateTime(),
                    transaction.getPerformTime(),
                    transaction.getCancelTime(),
                    transaction.getId(),
                    transaction.getState().getCode(),
                    transaction.getReason() != null ? transaction.getReason().getCode() : null);
        }
        else {

            throw new TransactionNotFoundException("Transaction not found.");
        }
    }

    @Override
    public Transactions getStatement(Date from, Date to) {

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
                            transaction.getId(),
                            transaction.getState().getCode(),
                            transaction.getReason() != null ? transaction.getReason().getCode() : null
                    ))
                    .collect(Collectors.toList());
        }

        return new Transactions(results);
    }
}