package woozlabs.echo.domain.gmail.dto.thread;

import lombok.Builder;
import lombok.Data;
import woozlabs.echo.global.dto.ResponseDto;

import java.util.List;

@Data
@Builder
public class GmailThreadUpdateResponse implements ResponseDto {
    private List<String> addLabelIds;
    private List<String> removeLabelIds;
}