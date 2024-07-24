package woozlabs.echo.domain.gmail.dto.thread;

import lombok.Data;

@Data
public class GmailThreadGetMessagesCc {
    private String ccName;
    private String ccEmail;
}
