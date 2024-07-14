package woozlabs.echo.global.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(CustomErrorException.class)
    protected ResponseEntity<CustomErrorResponse> handleCustomException(CustomErrorException e) {
        return CustomErrorResponse.toResponseEntity(e.getErrorCode(), e.getMessage());
    }
}
