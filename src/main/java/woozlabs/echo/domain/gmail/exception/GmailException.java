package woozlabs.echo.domain.gmail.exception;

public class GmailException extends RuntimeException{
    @Override
    public String getMessage() {
        return super.getMessage();
    }

    public GmailException(String message) {
        super(message);
    }
}
