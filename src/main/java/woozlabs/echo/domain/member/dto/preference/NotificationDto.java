package woozlabs.echo.domain.member.dto.preference;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {

    private String watchNotification; // ‘INBOX’ | ‘IMPORTANT’ | string
    private Boolean marketingEmails;
    private Boolean securityEmails;
}
