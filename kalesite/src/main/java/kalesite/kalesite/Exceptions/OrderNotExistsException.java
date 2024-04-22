package kalesite.kalesite.Exceptions;

import lombok.Getter;

@Getter
public class OrderNotExistsException extends Exception {

    private final int code;
    private final String data;

    public OrderNotExistsException() {

        super("Order not found");
        this.code = -31050;
        this.data = "order";
    }

    public OrderNotExistsException(String message) {

        super(message);
        this.code = -31050;
        this.data = "order";
    }

    public int getCode() {
        return code;
    }

    public String getData() {
        return data;
    }
}