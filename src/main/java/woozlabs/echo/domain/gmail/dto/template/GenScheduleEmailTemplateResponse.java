package woozlabs.echo.domain.gmail.dto.template;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import woozlabs.echo.global.dto.ResponseDto;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GenScheduleEmailTemplateResponse implements ResponseDto {
    private String template = "";
    private Boolean isSchedule = false;
}