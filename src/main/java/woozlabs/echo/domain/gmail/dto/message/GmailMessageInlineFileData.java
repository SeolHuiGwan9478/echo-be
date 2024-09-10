package woozlabs.echo.domain.gmail.dto.message;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GmailMessageInlineFileData {
    private String contentId;
    private String data;
}