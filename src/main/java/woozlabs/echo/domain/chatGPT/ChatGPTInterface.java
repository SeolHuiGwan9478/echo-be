package woozlabs.echo.domain.chatGPT;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import woozlabs.echo.domain.chatGPT.dto.ChatGPTRequest;
import woozlabs.echo.domain.chatGPT.dto.ChatGPTResponse;

@HttpExchange("/v1")
public interface ChatGPTInterface {

    @PostExchange("/chat/completions")
    ChatGPTResponse getChatCompletion(@RequestBody ChatGPTRequest request);
}
