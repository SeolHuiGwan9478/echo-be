package woozlabs.echo.domain.gmail.dto.history;

import com.google.api.services.gmail.model.HistoryMessageDeleted;
import com.google.api.services.gmail.model.Message;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GmailHistoryListMessageDeleted {
    private String id;
    private String threadId;

    public static GmailHistoryListMessageDeleted toGmailHistoryListMessageDeleted(HistoryMessageDeleted dto){
        Message message = dto.getMessage();
        return GmailHistoryListMessageDeleted.builder()
                .id(message.getId())
                .threadId(message.getThreadId())
                .build();
    }
}