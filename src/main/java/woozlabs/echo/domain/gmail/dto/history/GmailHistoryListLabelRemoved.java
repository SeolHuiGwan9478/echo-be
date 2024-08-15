package woozlabs.echo.domain.gmail.dto.history;

import com.google.api.services.gmail.model.HistoryLabelRemoved;
import lombok.Data;

import java.util.List;

@Data
public class GmailHistoryListLabelRemoved {
    private String id;
    private String threadId;
    private List<String> labelIds;

    public static GmailHistoryListLabelRemoved toGmailHistoryListLabelRemoved(HistoryLabelRemoved dto){
        GmailHistoryListLabelRemoved response = new GmailHistoryListLabelRemoved();
        response.setId(dto.getMessage().getId());
        response.setThreadId(dto.getMessage().getThreadId());
        response.setLabelIds(dto.getMessage().getLabelIds());
        return response;
    }
}
