package woozlabs.echo.domain.echo.dto.emailTemplate;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CreateEmailTemplateRequest {

    private String templateName;
    private String subject;
    private List<String> to = new ArrayList<>();
    private String body;
}
