package woozlabs.echo.domain.gmail.dto.thread;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class GmailThreadListThreads implements Comparable<GmailThreadListThreads> {
    private String id;
    private BigInteger historyId;
    private List<String> labelIds;
    private int attachmentSize;
    private List<GmailThreadListAttachments> attachments;
    private List<GmailThreadGetMessages> messages;
    private List<String> fromName;
    private List<String> fromEmail;
    private String subject;
    private String snippet;
    private Boolean verification = Boolean.FALSE;
    private List<String> codes;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime internalDate;
    private int threadSize;

    @Override
    public int compareTo(GmailThreadListThreads o) {
        if (internalDate.isBefore(o.getInternalDate())) return 1;
        else if (internalDate.isAfter(o.getInternalDate())) return -1;
        else return 0;
    }

    public void updateVerification(){
        this.verification = Boolean.TRUE;
    }
}