package woozlabs.echo.domain.gmail.dto.thread;

import lombok.Builder;
import lombok.Data;
import woozlabs.echo.global.dto.ResponseDto;

@Data
@Builder
public class GmailThreadTotalCountResponse implements ResponseDto {
    private int totalCount;
}
