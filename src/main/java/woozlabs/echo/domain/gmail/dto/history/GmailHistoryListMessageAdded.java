package woozlabs.echo.domain.gmail.dto.history;

import com.google.api.services.gmail.model.HistoryMessageAdded;
import lombok.Data;

@Data
public class GmailHistoryListMessageAdded {
    private String id;
    private String threadId;

    public static GmailHistoryListMessageAdded toGmailHistoryListMessageAdded(HistoryMessageAdded dto){
        GmailHistoryListMessageAdded response = new GmailHistoryListMessageAdded();
        response.setId(dto.getMessage().getId());
        response.setThreadId(dto.getMessage().getThreadId());
        return response;
    }
}