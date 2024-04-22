package kalesite.kalesite.Exceptions;

public class JsonRpcError {

    private JsonRpcException exception;
    private int code;
    private String message;
    private String data;

    public JsonRpcError(JsonRpcException exception, int code, String message, String data) {
        this.exception = exception;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // Getters and setters for exception, code, message, and data
    public JsonRpcException getException() {
        return exception;
    }

    public void setException(JsonRpcException exception) {
        this.exception = exception;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}