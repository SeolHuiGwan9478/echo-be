package woozlabs.echo.domain.gemini.dto;

import lombok.Data;

@Data
public class ChangeToneRequest {

    String contents;
    String parts;
    String tone;
}
