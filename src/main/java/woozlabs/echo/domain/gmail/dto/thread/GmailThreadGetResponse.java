package woozlabs.echo.domain.gmail.dto.thread;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import woozlabs.echo.global.dto.ResponseDto;

import java.math.BigInteger;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GmailThreadGetResponse implements ResponseDto {
    private String id;
    private BigInteger historyId;
    private List<String> labelIds;
    private int attachmentSize;
    private List<GmailThreadListAttachments> attachments;
    private int inlineImageSize;
    private List<GmailThreadListInlineImages> inlineImages;
    private List<GmailThreadGetMessagesResponse> messages;
    private List<GmailThreadGetMessagesFrom> from;
    private List<GmailThreadGetMessagesCc> cc;
    private List<GmailThreadGetMessagesBcc> bcc;
    private String subject;
    private String snippet;
    private Long timestamp;
    private int threadSize;
}