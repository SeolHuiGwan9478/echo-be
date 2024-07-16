package woozlabs.echo.domain.gmail.dto.thread;

import lombok.Data;

import java.util.List;

@Data
public class GmailThreadUpdateRequest {
    private List<String> addLabelIds;
    private List<String> removeLabelIds;
}
