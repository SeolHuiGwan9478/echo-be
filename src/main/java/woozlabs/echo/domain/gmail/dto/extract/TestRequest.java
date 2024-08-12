package woozlabs.echo.domain.gmail.dto.extract;

import lombok.Data;

import java.util.List;

@Data
public class TestRequest {
    private String text;
    private List<String> dates;
}
