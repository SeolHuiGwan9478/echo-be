package woozlabs.echo.domain.gmail.dto.message;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GmailMessageInlineImage {
    private String mimeType;
    private byte[] data;
}
