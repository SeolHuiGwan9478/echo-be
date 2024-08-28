package woozlabs.echo.domain.member.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationDto {

    private String watchNotification; // ‘INBOX’ | ‘IMPORTANT’ | string
    private boolean marketingEmails;
    private boolean securityEmails;
}
