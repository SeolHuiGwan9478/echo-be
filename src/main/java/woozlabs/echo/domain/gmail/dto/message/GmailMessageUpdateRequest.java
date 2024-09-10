package woozlabs.echo.domain.gmail.dto.message;

import lombok.Data;

import java.util.List;

@Data
public class GmailMessageUpdateRequest {
    private List<String> addLabelIds;
    private List<String> removeLabelIds;
}
