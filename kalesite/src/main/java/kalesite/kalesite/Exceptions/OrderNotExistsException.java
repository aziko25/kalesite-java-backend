package kalesite.kalesite.Exceptions;

public class OrderNotExistsException extends Exception {

    public OrderNotExistsException() {
        super();
    }

    public OrderNotExistsException(String message) {
        super(message);
    }
}
