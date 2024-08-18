package woozlabs.echo.domain.gmail.dto.extract;

import lombok.Data;

import java.util.List;

@Data
public class GenScheduleEmailTemplateRequest {
    private String content;
    private List<String> availableDates;
}