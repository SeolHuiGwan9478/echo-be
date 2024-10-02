package woozlabs.echo.domain.gmail.dto.template;

import lombok.Data;
@Data
public class GenScheduleEmailTemplateRequest {
    private String threadId;
    private String toEmail;
    private String content;
}