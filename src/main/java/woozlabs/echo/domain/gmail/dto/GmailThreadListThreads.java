package woozlabs.echo.domain.gmail.dto;

import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class GmailThreadListThreads {
    private String id;
    private BigInteger historyId;
    private List<String> labelIds;
    private String mimeType;
    private int attachmentSize;
    private List<GmailThreadListAttachments> attachments;
    private String from;
    private String subject;
    private String snippet;
    private String date;
    private int threadSize;
}