package woozlabs.echo.domain.gmail.dto.thread;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GmailThreadListInlineImages {
    private String mimeType;
    private String fileName;
    private String attachmentId;
    private int size;
}
