package woozlabs.echo.domain.gmail.dto.history;

import com.google.api.services.gmail.model.HistoryLabelAdded;
import com.google.api.services.gmail.model.Message;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GmailHistoryListLabelAdded {
    private String id;
    private String threadId;
    private List<String> labelIds;

    public static GmailHistoryListLabelAdded toGmailHistoryListLabelAdded(HistoryLabelAdded dto){
        Message message = dto.getMessage();
        return GmailHistoryListLabelAdded.builder()
                .id(message.getId())
                .threadId(message.getThreadId())
                .labelIds(message.getLabelIds())
                .build();
    }
}