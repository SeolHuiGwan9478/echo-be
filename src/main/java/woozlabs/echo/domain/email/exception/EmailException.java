package woozlabs.echo.domain.email.exception;

public class EmailException extends RuntimeException{
    @Override
    public String getMessage() {
        return super.getMessage();
    }

    public EmailException(String message) {
        super(message);
    }
}
