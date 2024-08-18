package woozlabs.echo.domain.gmail.dto.history;

import com.google.api.services.gmail.model.HistoryLabelRemoved;
import com.google.api.services.gmail.model.Message;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GmailHistoryListLabelRemoved {
    private String id;
    private String threadId;
    private List<String> labelIds;

    public static GmailHistoryListLabelRemoved toGmailHistoryListLabelRemoved(HistoryLabelRemoved dto){
        Message message = dto.getMessage();
        return GmailHistoryListLabelRemoved.builder()
                .id(message.getId())
                .threadId(message.getThreadId())
                .labelIds(message.getLabelIds())
                .build();
    }
}
