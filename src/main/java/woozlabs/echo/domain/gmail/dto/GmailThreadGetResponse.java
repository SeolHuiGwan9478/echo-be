package woozlabs.echo.domain.gmail.dto;


import lombok.Builder;
import lombok.Getter;
import woozlabs.echo.global.dto.ResponseDto;

import java.math.BigInteger;
import java.util.List;

@Getter
@Builder
public class GmailThreadGetResponse implements ResponseDto {
    private String id;
    private BigInteger historyId;
    private List<GmailThreadGetMessages> messages;
}
