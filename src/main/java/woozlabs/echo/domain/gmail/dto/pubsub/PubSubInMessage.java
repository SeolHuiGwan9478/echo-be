package woozlabs.echo.domain.gmail.dto.pubsub;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PubSubInMessage {
    private String data;
    private String messageId;
    private String publishTime;
}
