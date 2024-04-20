package kalesite.kalesite.Exceptions;

public class TransactionNotFoundException extends Exception {
    public TransactionNotFoundException() {
        super();
    }

    public TransactionNotFoundException(String message) {
        super(message);
    }
}
