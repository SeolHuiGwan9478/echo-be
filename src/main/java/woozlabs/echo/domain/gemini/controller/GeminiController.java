package woozlabs.echo.domain.gemini.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import woozlabs.echo.domain.gemini.service.GeminiService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/gemini/")
public class GeminiController {

    private final GeminiService geminiService;

    @PostMapping("/completion")
    public ResponseEntity<String> getCompletion(@RequestBody String text) {
        String completion = geminiService.getCompletion(text);
        return ResponseEntity.ok(completion);
    }
}
