package woozlabs.echo.domain.gmail.dto;

import lombok.Builder;
import lombok.Getter;
import woozlabs.echo.global.dto.ResponseDto;

@Getter
@Builder
public class GmailMessageSendResponse implements ResponseDto {
    private String id;
}
