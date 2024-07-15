package woozlabs.echo.domain.gmail.dto.draft;

import lombok.Builder;
import lombok.Getter;
import woozlabs.echo.global.dto.ResponseDto;

@Getter
@Builder
public class GmailDraftGetResponse implements ResponseDto {
    private String id;
    private GmailDraftGetMessage message;
}
