package woozlabs.echo.domain.gmail.dto.thread;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GmailThreadGetMessagesFrom {
    private String fromName;
    private String fromEmail;
}