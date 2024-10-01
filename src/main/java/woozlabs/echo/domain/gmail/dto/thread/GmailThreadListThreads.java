package woozlabs.echo.domain.gmail.dto.thread;

import lombok.Data;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@Data
public class GmailThreadListThreads {
    private String id;
    private String subject;
    private String snippet;
    private Long timestamp;
    private BigInteger historyId;
    private List<String> labelIds;
    private int threadSize;
    private List<GmailThreadGetMessagesFrom> from;
    private List<GmailThreadGetMessagesCc> cc;
    private List<GmailThreadGetMessagesBcc> bcc;
    private int attachmentSize;
    private Map<String, GmailThreadListAttachments> attachments;
    private int googleDriveAttachmentSize;
    private List<String> googleDriveAttachments;
    private List<GmailThreadGetMessagesResponse> messages;
}