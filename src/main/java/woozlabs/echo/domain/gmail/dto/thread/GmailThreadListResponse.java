package woozlabs.echo.domain.gmail.dto.thread;

import lombok.Builder;
import lombok.Getter;
import woozlabs.echo.global.dto.ResponseDto;

import java.util.List;

@Getter
@Builder
public class GmailThreadListResponse implements ResponseDto {
    private List<GmailThreadListThreads> threads;
    private String nextPageToken;
}
