package woozlabs.echo.domain.gmail.dto.thread;

import lombok.Data;

@Data
public class GmailThreadGetMessagesBcc {
    private String bccName;
    private String bccEmail;
}