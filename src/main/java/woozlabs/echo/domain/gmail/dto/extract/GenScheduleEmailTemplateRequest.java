package woozlabs.echo.domain.gmail.dto.extract;

import lombok.Data;
@Data
public class GenScheduleEmailTemplateRequest {
    private String fromEmail;
    private String content;
}