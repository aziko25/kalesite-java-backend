package kalesite.kalesite.Exceptions;

public class JsonRpcException extends Exception {

    public JsonRpcException() {
        super();
    }

    public JsonRpcException(String message) {
        super(message);
    }

    public JsonRpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public JsonRpcException(Throwable cause) {
        super(cause);
    }
}
