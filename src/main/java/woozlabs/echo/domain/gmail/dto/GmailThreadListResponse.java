package woozlabs.echo.domain.gmail.dto;

import lombok.Builder;
import lombok.Getter;
import woozlabs.echo.global.dto.ResponseDto;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

@Getter
@Builder
public class GmailThreadListResponse implements ResponseDto {
    private List<GmailThreadListThreads> threads;
    private String nextPageToken;
}
