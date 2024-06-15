package woozlabs.echo.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CustomErrorException extends RuntimeException {
    private final ErrorCode errorCode;
}
