package woozlabs.echo.domain.member.dto.preference;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import woozlabs.echo.domain.member.entity.Watch;

import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {

    private Map<String, Watch> watchNotification; // Map<String, "INBOX" | "IMPORTANT" | "OFF">
    private Boolean marketingEmails;
    private Boolean securityEmails;
}
