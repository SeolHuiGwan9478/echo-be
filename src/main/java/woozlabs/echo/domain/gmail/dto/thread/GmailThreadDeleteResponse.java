package woozlabs.echo.domain.gmail.dto.thread;

import lombok.AllArgsConstructor;
import lombok.Getter;
import woozlabs.echo.global.dto.ResponseDto;

@Getter
@AllArgsConstructor
public class GmailThreadDeleteResponse implements ResponseDto {
    private String id;
}
