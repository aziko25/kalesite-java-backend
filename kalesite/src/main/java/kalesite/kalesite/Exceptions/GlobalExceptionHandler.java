package kalesite.kalesite.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnableCompleteException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleUnableCompleteException(UnableCompleteException e) {

        System.out.println("handler is on the deal");

        return new ErrorResponse(e.getCode(), e.getMessage(), e.getData());
    }

    static class ErrorResponse {

        private int code;
        private String message;
        private String data;

        public ErrorResponse(int code, String message, String data) {
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
    }
}