package woozlabs.echo.domain.gmail.dto;

import lombok.Data;

@Data
public class GmailMessageSendRequest {
    private String toEmailAddress;
    private String fromEmailAddress;
    private String subject;
    private String bodyText;
}