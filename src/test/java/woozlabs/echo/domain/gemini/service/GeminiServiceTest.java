package woozlabs.echo.domain.gemini.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class GeminiServiceTest {

    @Autowired
    private GeminiService geminiService;

    @Test
    public void getCompetion() throws Exception {
        String text = geminiService.getCompletion("서울 맛집을 추천해줘");
        System.out.println(text);
    }

    @Test
    public void summarizeGmailThreadExample() throws Exception {
        // given
        String threadContent = "{\n" +
                "  \"threadContent\": \"From: john@example.com\\n" +
                "To: team@company.com\\nSubject: Project Update\\n" +
                "\\nHi team,\\n\\nJust a quick update on our current project:\\n\\n" +
                "1. We've completed the initial design phase.\\n" +
                "2. The development team will start coding next week.\\n" +
                "3. We're still waiting for feedback from the client on the color scheme.\\n\\n" +
                "Please let me know if you have any questions or concerns.\\n\\n" +
                "Best regards,\\n" +
                "John\\n\\n" +
                "--- \\n" +
                "From: sarah@company.com\\n" +
                "To: john@example.com, team@company.com\\n" +
                "Subject: Re: Project Update\\n\\n" +
                "Hi John,\\n\\n" +
                "Thanks for the update. A couple of questions:\\n\\n" +
                "- When is our next team meeting scheduled?\\n" +
                "- Do we have a timeline for the testing phase?\\n\\n" +
                "Regards,\\n" +
                "Sarah\\n\\n" +
                "---\\n" +
                "From: john@example.com\\n" +
                "To: sarah@company.com, team@company.com\\n" +
                "Subject: Re: Project Update\\n\\n" +
                "Hi Sarah,\\n\\n" +
                "Good questions:\\n\\n" +
                "1. Our next team meeting is this Friday at 2 PM.\\n" +
                "2. We're aiming to start testing in about 3 weeks, assuming development goes as planned.\\n\\n" +
                "Let me know if you need any more information.\\n\\n" +
                "Best,\\n" +
                "John\"\n" +
                "}";
        // when
        String summary = geminiService.summarizeGmailThread(threadContent);

        // then
        System.out.println("Summary: " + summary);

    }

}