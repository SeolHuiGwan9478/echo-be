package woozlabs.echo.domain.gmail.dto.pubsub;

import com.google.api.services.gmail.model.HistoryMessageAdded;
import com.google.api.services.gmail.model.Message;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MessageInHistoryData {
    private final String threadId;
    private final String id;

    public static MessageInHistoryData toMessageInHistoryData(Message message){
        return MessageInHistoryData.builder()
                .id(message.getId())
                .threadId(message.getThreadId())
                .build();
    }
}
