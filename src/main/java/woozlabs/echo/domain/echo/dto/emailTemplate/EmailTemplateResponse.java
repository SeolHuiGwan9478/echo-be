package woozlabs.echo.domain.echo.dto.emailTemplate;

import lombok.*;
import woozlabs.echo.domain.echo.entity.EmailRecipient;
import woozlabs.echo.domain.echo.entity.EmailTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailTemplateResponse {

    private Long key;
    private String templateName;
    private String subject;
    private List<String> to = new ArrayList<>();
    private String body;

    public EmailTemplateResponse(EmailTemplate emailTemplate) {
        this.key = emailTemplate.getId();
        this.templateName = emailTemplate.getTemplateName();
        this.subject = emailTemplate.getSubject();
        this.to = emailTemplate.getRecipients().stream()
                .map(EmailRecipient::getEmail)
                .collect(Collectors.toList());
        this.body = emailTemplate.getBody();
    }
}
