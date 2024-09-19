package woozlabs.echo.domain.gmail.dto.autoForwarding;

import lombok.Data;

import java.util.List;

@Data
public class AutoForwardingRequest {
    List<AutoForwardingData> autoForwardingData;
}
