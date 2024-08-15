package woozlabs.echo.domain.gmail.dto.pubsub;

import com.google.api.services.gmail.model.HistoryMessageAdded;
import com.google.api.services.gmail.model.Message;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MessageInHistoryData {
    private String id;
    private String threadId;
    private HistoryType historyType;
    public static MessageInHistoryData toMessageInHistoryData(Message message, HistoryType type){
        return MessageInHistoryData.builder()
                .id(message.getId())
                .threadId(message.getThreadId())
                .historyType(type)
                .build();
    }
}
