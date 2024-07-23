package woozlabs.echo.domain.gmail.dto.pubsub;

import lombok.Data;

@Data
public class PubSubInMessage {
    private String data;
    private String messageId;
    private String message_id;
    private String publishTime;
    private String publish_time;
}
