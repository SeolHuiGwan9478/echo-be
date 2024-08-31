package woozlabs.echo.global.exceptionHandler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import woozlabs.echo.domain.gmail.exception.GmailException;
import woozlabs.echo.global.constant.GlobalConstant;
import woozlabs.echo.global.dto.ErrorResponse;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.CustomErrorResponse;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(CustomErrorException.class)
    protected ResponseEntity<CustomErrorResponse> handleCustomException(CustomErrorException e) {
        return CustomErrorResponse.toResponseEntity(e.getErrorCode(), e.getMessage());
    }
    @ExceptionHandler(GmailException.class)
    protected ResponseEntity<ErrorResponse> handleEmailException(GmailException ex){
        Map<String, String> errors = new HashMap<>();
        errors.put(GlobalConstant.EMAIL_ERR_MSG_KEY, ex.getMessage());
        final ErrorResponse errorResponse = new ErrorResponse(errors);
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}