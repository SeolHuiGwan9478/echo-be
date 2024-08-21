package woozlabs.echo.domain.gmail.dto.pubsub;

import com.google.api.services.gmail.model.HistoryMessageAdded;
import com.google.api.services.gmail.model.Message;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MessageInHistoryData {
    private String id;
    private String threadId;
    private HistoryType historyType;
    private List<String> labelIds;
    public static MessageInHistoryData toMessageInHistoryData(Message message, HistoryType type, List<String> labelIds){
        return MessageInHistoryData.builder()
                .id(message.getId())
                .threadId(message.getThreadId())
                .historyType(type)
                .labelIds(labelIds)
                .build();
    }
}
