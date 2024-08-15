package woozlabs.echo.domain.gmail.dto.history;

import com.google.api.services.gmail.model.HistoryMessageAdded;
import com.google.api.services.gmail.model.HistoryMessageDeleted;
import lombok.Data;

@Data
public class GmailHistoryListMessageDeleted {
    private String id;
    private String threadId;

    public static GmailHistoryListMessageDeleted toGmailHistoryListMessageDeleted(HistoryMessageDeleted dto){
        GmailHistoryListMessageDeleted response = new GmailHistoryListMessageDeleted();
        response.setId(dto.getMessage().getId());
        response.setThreadId(dto.getMessage().getThreadId());
        return response;
    }
}
