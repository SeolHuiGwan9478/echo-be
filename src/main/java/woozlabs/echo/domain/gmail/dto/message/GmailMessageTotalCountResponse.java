package woozlabs.echo.domain.gmail.dto.message;

import lombok.Builder;
import lombok.Data;
import woozlabs.echo.global.dto.ResponseDto;

@Data
@Builder
public class GmailMessageTotalCountResponse implements ResponseDto {
    private int totalCount;
}
