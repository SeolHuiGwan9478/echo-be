package woozlabs.echo.domain.email.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import woozlabs.echo.global.dto.ResponseDto;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class GetUserEmailMessagesResponse implements ResponseDto {

}