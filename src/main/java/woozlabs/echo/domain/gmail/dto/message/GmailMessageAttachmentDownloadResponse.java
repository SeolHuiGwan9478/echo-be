package woozlabs.echo.domain.gmail.dto.message;

import lombok.Builder;
import lombok.Data;
import org.springframework.core.io.ByteArrayResource;

@Data
@Builder
public class GmailMessageAttachmentDownloadResponse {
    private String attachmentId;
    private int size;
    private byte[] byteData;
    private ByteArrayResource resource;
}
