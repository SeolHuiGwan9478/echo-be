package woozlabs.echo.domain.gmail.dto.thread;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

@Data
@Builder
public class GmailThreadListAttachments{
    private String mimeType;
    private String fileName;
    private String attachmentId;
    private int size;
}