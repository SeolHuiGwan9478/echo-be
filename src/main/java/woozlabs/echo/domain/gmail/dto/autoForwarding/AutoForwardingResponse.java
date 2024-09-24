package woozlabs.echo.domain.gmail.dto.autoForwarding;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AutoForwardingResponse {
    private String q;
    private String forwardingEmail;
}