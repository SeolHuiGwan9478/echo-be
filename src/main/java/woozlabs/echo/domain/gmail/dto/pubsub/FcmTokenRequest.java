package woozlabs.echo.domain.gmail.dto.pubsub;

import lombok.Data;

@Data
public class FcmTokenRequest {
    private String fcmToken;
    private String machineUuid;
}
