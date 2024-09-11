package woozlabs.echo.domain.gmail.dto.pubsub;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetVerificationDataResponse {
    private String uuid;
    private String links;
    private String codes;
    private String shortenedLink;
}
