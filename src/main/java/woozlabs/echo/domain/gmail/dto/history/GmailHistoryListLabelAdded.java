package woozlabs.echo.domain.gmail.dto.history;

import com.google.api.services.gmail.model.HistoryLabelAdded;
import com.google.api.services.gmail.model.HistoryLabelRemoved;
import lombok.Data;

import java.util.List;

@Data
public class GmailHistoryListLabelAdded {
    private String id;
    private String threadId;
    private List<String> labelIds;

    public static GmailHistoryListLabelAdded toGmailHistoryListLabelRAdded(HistoryLabelAdded dto){
        GmailHistoryListLabelAdded response = new GmailHistoryListLabelAdded();
        response.setId(dto.getMessage().getId());
        response.setThreadId(dto.getMessage().getThreadId());
        response.setLabelIds(dto.getMessage().getLabelIds());
        return response;
    }
}
