package woozlabs.echo.domain.gmail.dto.pubsub;

import lombok.Data;

@Data
public class PubSubNotification {
    private String emailAddress;
    private String historyId;
}