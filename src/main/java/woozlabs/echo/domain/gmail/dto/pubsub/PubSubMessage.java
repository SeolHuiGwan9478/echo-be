package woozlabs.echo.domain.gmail.dto.pubsub;

import lombok.Data;

@Data
public class PubSubMessage {
    private PubSubInMessage message;
    private String subscription;
}
