package woozlabs.echo.domain.gmail.dto.history;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GmailHistoryListData {
    private List<GmailHistoryListMessageAdded> messagesAdded;
    private List<GmailHistoryListMessageDeleted> messagesDeleted;
    private List<GmailHistoryListLabelAdded> labelsAdded;
    private List<GmailHistoryListLabelRemoved> labelsRemoved;
}