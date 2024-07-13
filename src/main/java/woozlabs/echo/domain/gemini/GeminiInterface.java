package woozlabs.echo.domain.gemini;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import woozlabs.echo.domain.gemini.dto.GeminiRequest;
import woozlabs.echo.domain.gemini.dto.GeminiResponse;

@HttpExchange("/v1beta/models/")
public interface GeminiInterface {

    @PostExchange("{model}:generateContent")
    GeminiResponse getCompletion(
            @PathVariable("model") String model,
            @RequestBody GeminiRequest request
    );
}
