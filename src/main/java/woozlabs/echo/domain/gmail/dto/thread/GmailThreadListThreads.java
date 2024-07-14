package woozlabs.echo.domain.gmail.dto.thread;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class GmailThreadListThreads implements Comparable<GmailThreadListThreads> {
    private String id;
    private BigInteger historyId;
    private List<String> labelIds;
    private String mimeType;
    private int attachmentSize;
    private List<GmailThreadListAttachments> attachments;
    private List<String> fromName;
    private List<String> fromEmail;
    private String subject;
    private String snippet;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime internalDate;
    private int threadSize;

    @Override
    public int compareTo(GmailThreadListThreads o) {
        if (internalDate.isBefore(o.getInternalDate())) return 1;
        else if (internalDate.isAfter(o.getInternalDate())) return -1;
        else return 0;
    }
}