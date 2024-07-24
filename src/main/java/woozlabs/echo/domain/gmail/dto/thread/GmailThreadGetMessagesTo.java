package woozlabs.echo.domain.gmail.dto.thread;

import lombok.Data;

@Data
public class GmailThreadGetMessagesTo {
    private String toName;
    private String toEmail;
}
