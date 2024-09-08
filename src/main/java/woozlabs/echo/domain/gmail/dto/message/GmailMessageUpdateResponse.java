package woozlabs.echo.domain.gmail.dto.message;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GmailMessageUpdateResponse {
    private List<String> addLabelIds;
    private List<String> removeLabelIds;
}
