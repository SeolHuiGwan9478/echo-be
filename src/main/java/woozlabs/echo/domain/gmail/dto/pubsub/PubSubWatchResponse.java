package woozlabs.echo.domain.gmail.dto.pubsub;

import lombok.Builder;
import lombok.Getter;
import woozlabs.echo.global.dto.ResponseDto;

import java.math.BigInteger;

@Getter
@Builder
public class PubSubWatchResponse implements ResponseDto {
    private BigInteger historyId;
    private Long expiration;
}