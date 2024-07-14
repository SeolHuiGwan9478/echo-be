package woozlabs.echo.domain.gmail.dto.message;

import lombok.Builder;
import lombok.Getter;
import woozlabs.echo.global.dto.ResponseDto;

import java.util.List;

@Getter
@Builder
public class GmailMessageSendResponse implements ResponseDto {
    private String id;
    private String threadId;
    private List<String> labelsId;
    private String snippet;
}
