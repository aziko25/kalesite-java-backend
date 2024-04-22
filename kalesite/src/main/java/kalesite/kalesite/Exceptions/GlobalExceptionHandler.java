package kalesite.kalesite.Exceptions;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Configuration
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(value = OrderNotExistsException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotExistsException(OrderNotExistsException ex) {
        log.error("Handling OrderNotExistsException: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(ex.getCode(), ex.getMessage(), ex.getData());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
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