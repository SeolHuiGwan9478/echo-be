package woozlabs.echo.domain.gmail.dto.history;

import com.google.api.services.gmail.model.HistoryMessageAdded;
import com.google.api.services.gmail.model.Message;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GmailHistoryListMessageAdded {
    private String id;
    private String threadId;

    public static GmailHistoryListMessageAdded toGmailHistoryListMessageAdded(HistoryMessageAdded dto){
        Message message = dto.getMessage();
        return GmailHistoryListMessageAdded.builder()
                .id(message.getId())
                .threadId(message.getThreadId())
                .build();
    }
}
