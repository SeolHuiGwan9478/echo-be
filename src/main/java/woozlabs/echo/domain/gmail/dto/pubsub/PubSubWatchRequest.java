package woozlabs.echo.domain.gmail.dto.pubsub;

import lombok.Data;

import java.util.List;

@Data
public class PubSubWatchRequest {
    private List<String> labelIds;
}