package kalesite.kalesite.Exceptions;

import lombok.Getter;

@Getter
public class OrderNotExistsException extends Exception {

    private int code;
    private String data;

    public OrderNotExistsException() {

        super("Order not found");
        this.code = -31050;
        this.data = "order";
    }

    public OrderNotExistsException(String message) {
        super(message);
    }
}
