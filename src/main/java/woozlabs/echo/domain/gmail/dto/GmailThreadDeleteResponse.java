package woozlabs.echo.domain.gmail.dto;

import com.google.api.services.gmail.model.Thread;
import lombok.Getter;
import woozlabs.echo.global.dto.ResponseDto;

import java.math.BigInteger;

@Getter
public class GmailThreadDeleteResponse implements ResponseDto {
    private final String id;
    private final String snippet;
    private final BigInteger historyId;

    public GmailThreadDeleteResponse(Thread thread){
        this.id = thread.getId();
        this.snippet = thread.getSnippet();
        this.historyId = thread.getHistoryId();
    }
}