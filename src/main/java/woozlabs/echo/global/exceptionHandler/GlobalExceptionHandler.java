package woozlabs.echo.global.exceptionHandler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import woozlabs.echo.domain.email.exception.EmailException;
import woozlabs.echo.global.constant.GlobalConstant;
import woozlabs.echo.global.dto.ErrorResponse;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(EmailException.class)
    protected ResponseEntity<ErrorResponse> handleEmailException(EmailException ex){
        Map<String, String> errors = new HashMap<>();
        errors.put(GlobalConstant.EMAIL_ERR_MSG_KEY, ex.getMessage());
        final ErrorResponse errorResponse = new ErrorResponse(errors);
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}