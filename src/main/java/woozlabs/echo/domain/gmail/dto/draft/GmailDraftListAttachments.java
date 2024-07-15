package woozlabs.echo.domain.gmail.dto.draft;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GmailDraftListAttachments {
    private String mimeType;
    private String fileName;
    private String attachmentId;
    private int size;
}
