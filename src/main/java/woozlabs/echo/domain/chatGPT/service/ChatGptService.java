package woozlabs.echo.domain.chatGPT.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import woozlabs.echo.domain.chatGPT.ChatGPTInterface;
import woozlabs.echo.domain.chatGPT.dto.ChatGPTRequest;
import woozlabs.echo.domain.chatGPT.dto.ChatGPTResponse;
import woozlabs.echo.domain.gemini.prompt.VerificationMailPrompt;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatGptService {

    private static final String GPT_4 = "gpt-4o-mini";

    private final ChatGPTInterface chatGPTInterface;

    private ChatGPTResponse getChatCompletion(ChatGPTRequest request) {
        try {
            return chatGPTInterface.getChatCompletion(request);
        } catch (Exception e) {
            log.error("Error while getting completion from ChatGPT", e);
            throw new CustomErrorException(ErrorCode.FAILED_TO_CHATGPT_COMPLETION, e.getMessage());
        }
    }

    public String getCompletion(String text) {
        ChatGPTRequest chatGPTRequest = new ChatGPTRequest(GPT_4, text);
        ChatGPTResponse response = getChatCompletion(chatGPTRequest);

        return response.getChoices()
                .stream()
                .findFirst()
                .map(choice -> choice.getMessage().getContent())
                .orElse(null);
    }

    public String analyzeVerificationEmail(String emailContent) {
        // Filtering 거쳐서 받아오기
        //String coreContent = extractCoreContent(emailContent);

        String prompt = VerificationMailPrompt.getPrompt(emailContent);
        return getCompletion(prompt);
    }

//    private String extractCoreContent(String htmlContent) {
//        Document doc = Jsoup.parse(htmlContent);
//
//        doc.select("style, script, head, title, meta, img").remove();
//        doc.select("table, tbody, tr, td, th, thead, tfoot").unwrap();
//        Elements coreElements = doc.select("div, p, h1, h2, h3, a, pre, span, td");
//
//        for (Element coreElement : coreElements) {
//            coreElement.clearAttributes();
//        }
//
//        return coreElements.toString();
//    }
}
