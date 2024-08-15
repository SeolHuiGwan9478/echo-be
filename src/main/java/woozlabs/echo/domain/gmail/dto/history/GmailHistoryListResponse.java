package woozlabs.echo.domain.gmail.dto.history;

import lombok.Builder;
import lombok.Data;
import woozlabs.echo.global.dto.ResponseDto;

import java.math.BigInteger;
import java.util.List;

@Data
@Builder
public class GmailHistoryListResponse implements ResponseDto {
    private List<GmailHistoryListData> history;
    private String nextPageToken;
    private BigInteger historyId;
}