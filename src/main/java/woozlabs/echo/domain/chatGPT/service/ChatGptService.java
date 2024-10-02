package woozlabs.echo.domain.chatGPT.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import woozlabs.echo.domain.chatGPT.ChatGPTInterface;
import woozlabs.echo.domain.chatGPT.dto.ChatGPTRequest;
import woozlabs.echo.domain.chatGPT.dto.ChatGPTResponse;
import woozlabs.echo.domain.chatGPT.prompt.EmailTemplateSuggestionPrompt;
import woozlabs.echo.domain.chatGPT.prompt.ScheduleEmailPrompt;
import woozlabs.echo.domain.chatGPT.prompt.EmailTemplatePrompt;
import woozlabs.echo.domain.gemini.prompt.VerificationMailPrompt;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;

import java.time.LocalDate;
import java.util.List;

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
        String prompt = VerificationMailPrompt.getPrompt(emailContent);
        return getCompletion(prompt);
    }

    public String analyzeScheduleEmail(String emailContent){
        LocalDate currentDateTime = LocalDate.now();
        String prompt = ScheduleEmailPrompt.getPrompt(emailContent, currentDateTime.toString());
        return getCompletion(prompt);
    }

    public String generateEmailTemplate(String emailContent, List<String> availableDates){
        String prompt = EmailTemplatePrompt.getPrompt(emailContent, availableDates);
        return getCompletion(prompt);
    }

    public String generateEmailTemplateSuggestion(String emailContent){
        String prompt = EmailTemplateSuggestionPrompt.getPrompt(emailContent);
        return getCompletion(prompt);
    }
}
