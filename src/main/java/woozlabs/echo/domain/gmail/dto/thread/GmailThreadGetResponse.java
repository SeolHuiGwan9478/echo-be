package woozlabs.echo.domain.gmail.dto.thread;

import lombok.*;
import woozlabs.echo.global.dto.ResponseDto;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GmailThreadGetResponse implements ResponseDto {
    private String id;
    private BigInteger historyId;
    private List<String> labelIds;
    private int attachmentSize;
    private Map<String, GmailThreadListAttachments> attachments;
    private int inlineImageSize;
    private Map<String, GmailThreadListInlineImages> inlineImages;
    private List<GmailThreadGetMessagesResponse> messages;
    private List<GmailThreadGetMessagesFrom> from;
    private List<GmailThreadGetMessagesCc> cc;
    private List<GmailThreadGetMessagesBcc> bcc;
    private String subject;
    private String snippet;
    private Long timestamp;
    private int threadSize;
}