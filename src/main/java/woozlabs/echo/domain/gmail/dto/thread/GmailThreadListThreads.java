package woozlabs.echo.domain.gmail.dto.thread;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class GmailThreadListThreads {
    private String id;
    private String subject;
    private String snippet;
    private String date;
    private BigInteger historyId;
    private List<String> labelIds;
    private int threadSize;
    private List<GmailThreadGetMessagesFrom> from;
    private List<GmailThreadGetMessagesCc> cc;
    private List<GmailThreadGetMessagesBcc> bcc;
    private int attachmentSize;
    private List<GmailThreadListAttachments> attachments;
    private List<GmailThreadGetMessagesResponse> messages;

    public void addLabel(String newLabel){
        this.labelIds.add(newLabel);
    }
}