package woozlabs.echo.domain.member.dto;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {

    private String watchNotification; // ‘INBOX’ | ‘IMPORTANT’ | string
    private boolean marketingEmails;
    private boolean securityEmails;
}
