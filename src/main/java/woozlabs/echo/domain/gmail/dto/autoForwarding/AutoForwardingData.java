package woozlabs.echo.domain.gmail.dto.autoForwarding;

import lombok.Data;

@Data
public class AutoForwardingData {
    private String forwardingEmailAddress;
    private String forwardingSubject;
}