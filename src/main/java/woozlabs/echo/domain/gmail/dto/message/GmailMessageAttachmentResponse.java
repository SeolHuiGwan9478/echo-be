package woozlabs.echo.domain.gmail.dto.message;


import lombok.Builder;
import lombok.Getter;
import woozlabs.echo.global.dto.ResponseDto;

@Getter
@Builder
public class GmailMessageAttachmentResponse implements ResponseDto {
    private String attachmentId;
    private int size;
    private String data;
    private String base64Src;
}
