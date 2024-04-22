package kalesite.kalesite.Exceptions;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotExistsException.class)
    public ResponseEntity<Object> handleOrderNotExistsException(OrderNotExistsException ex) {

        ErrorResponse errorResponse = new ErrorResponse(ex.getCode(), ex.getMessage(), ex.getData());

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}

@Getter
@Setter
class ErrorResponse {

    private ErrorDetail error;

    public ErrorResponse(int code, String message, String data) {
        this.error = new ErrorDetail(code, message, data);
    }

    public ErrorDetail getError() {

        return error;
    }

    public void setError(ErrorDetail error) {

        this.error = error;
    }

    @Getter
    @Setter
    static class ErrorDetail {

        private int code;
        private String message;
        private String data;

        public ErrorDetail(int code, String message, String data) {

            this.code = code;
            this.message = message;
            this.data = data;
        }
    }
}