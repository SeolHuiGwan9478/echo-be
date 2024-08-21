package woozlabs.echo.domain.gmail.dto.pubsub;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PubSubMessage {
    private PubSubInMessage message;
    private String subscription;
    private int deliveryAttempt;
}