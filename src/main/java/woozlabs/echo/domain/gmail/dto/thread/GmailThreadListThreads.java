package woozlabs.echo.domain.gmail.dto.thread;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class GmailThreadListThreads implements Comparable<GmailThreadListThreads> {
    private String id;
    private BigInteger historyId;
    private List<String> labelIds;
    private int attachmentSize;
    private List<GmailThreadListAttachments> attachments;
    private List<GmailThreadGetMessagesResponse> messages;
    private List<GmailThreadGetMessagesFrom> from;
    private List<GmailThreadGetMessagesCc> cc;
    private List<GmailThreadGetMessagesBcc> bcc;
    private String subject;
    private String snippet;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private String date;
    private int threadSize;

    @Override
    public int compareTo(GmailThreadListThreads o) {
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(this.date);
        OffsetDateTime otherOffsetDateTime = OffsetDateTime.parse(o.getDate());
        if (offsetDateTime.isBefore(otherOffsetDateTime)) return 1;
        else if (offsetDateTime.isAfter(otherOffsetDateTime)) return -1;
        else return 0;
    }

    public void addLabel(String newLabel){
        this.labelIds.add(newLabel);
    }
}