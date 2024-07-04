package woozlabs.echo.domain.gmail.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import woozlabs.echo.global.dto.ResponseDto;

@Getter
@AllArgsConstructor
public class GmailThreadTrashResponse implements ResponseDto {
    private final String id;
}