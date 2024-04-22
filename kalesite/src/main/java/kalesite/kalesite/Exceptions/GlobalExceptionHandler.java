package kalesite.kalesite.Exceptions;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnableCompleteException.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ErrorResponseWrapper handleUnableCompleteException(UnableCompleteException e) {

        ErrorResponse errorResponse = new ErrorResponse("2.0", e.getCode(), e.getMessage(), e.getData());
        return new ErrorResponseWrapper(errorResponse);
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    public ResponseEntity<Object> illegalArgumentException(IllegalArgumentException exception) {

        return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    static class ErrorResponseWrapper {

        private final ErrorResponse error;

        public ErrorResponseWrapper(ErrorResponse error) {
            this.error = error;
        }

        public ErrorResponse getError() {
            return error;
        }
    }

    static class ErrorResponse {

        private final String jsonrpc;
        private final int code;
        private final String message;
        private final String data;

        public ErrorResponse(String jsonrpc, int code, String message, String data) {
            this.jsonrpc = jsonrpc;
            this.code = code;
            this.message = message;
            this.data = data;
        }

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

        public String getData() {
            return data;
        }

        public String getJsonrpc() { return jsonrpc; }
    }
}