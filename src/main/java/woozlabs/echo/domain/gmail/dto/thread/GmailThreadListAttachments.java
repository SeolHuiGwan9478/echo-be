package woozlabs.echo.domain.gmail.dto.thread;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@EqualsAndHashCode(of = "xAttachmentId")
public class GmailThreadListAttachments {
    private String xAttachmentId;
    private String mimeType;
    private String fileName;
    private String attachmentId;
    private int size;
}