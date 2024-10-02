package woozlabs.echo.domain.gmail.dto.template;

import lombok.Data;

import java.util.List;

@Data
public class GenEmailTemplateSuggestionResponse {
    private List<String> suggestions;
}