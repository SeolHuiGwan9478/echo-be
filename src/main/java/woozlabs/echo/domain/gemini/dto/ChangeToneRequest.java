package woozlabs.echo.domain.gemini.dto;

import lombok.Data;

@Data
public class ChangeToneRequest {

    String text;
    String tone;
}
