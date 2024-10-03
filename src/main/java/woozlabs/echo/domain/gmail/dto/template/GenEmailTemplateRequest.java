package woozlabs.echo.domain.gmail.dto.template;

import lombok.Data;
@Data
public class GenEmailTemplateRequest {
    private String threadId;
    private String toEmail;
    private String content;
}