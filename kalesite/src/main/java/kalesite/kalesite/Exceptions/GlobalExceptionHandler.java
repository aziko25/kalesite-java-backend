package kalesite.kalesite.Exceptions;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
@ComponentScan
public class GlobalExceptionHandler {

    @ExceptionHandler(UnableCompleteException.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ErrorResponse handleUnableCompleteException(UnableCompleteException e) {

        System.out.println("handler is on the deal");

        return new ErrorResponse(e.getCode(), e.getMessage(), e.getData());
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    public ResponseEntity<Object> illegalArgumentException(IllegalArgumentException exception) {

        return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
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