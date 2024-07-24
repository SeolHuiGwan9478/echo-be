package woozlabs.echo.domain.gmail.dto.pubsub;

import lombok.AllArgsConstructor;
import lombok.Getter;
import woozlabs.echo.global.dto.ResponseDto;
@Getter
@AllArgsConstructor
public class FcmTokenResponse implements ResponseDto {
    private Long fcmTokenId;
}