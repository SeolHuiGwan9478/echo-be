package woozlabs.echo.domain.chatGPT.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import woozlabs.echo.domain.chatGPT.service.ChatGptService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chatGpt")
public class ChatGptController {

    private final ChatGptService chatGptService;

    @PostMapping("/completion")
    public ResponseEntity<String> getCompletion(@RequestBody String text) {
        String completion = chatGptService.getCompletion(text);
        return ResponseEntity.ok(completion);
    }

    @PostMapping("/gmail/verification")
    public ResponseEntity<String> analyzeEmail(@RequestBody String emailContent) {
        String result = chatGptService.analyzeVerificationEmail(emailContent);
        return ResponseEntity.ok(result);
    }
}
