package woozlabs.echo.domain.gmail.dto.draft;

import lombok.Builder;
import lombok.Getter;
import woozlabs.echo.global.dto.ResponseDto;

import java.util.List;

@Builder
@Getter
public class GmailDraftSendResponse implements ResponseDto {
    private String id;
    private String threadId;
    private List<String> labelsId;
    private String snippet;
}
