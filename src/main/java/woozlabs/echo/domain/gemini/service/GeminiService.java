package woozlabs.echo.domain.gemini.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import woozlabs.echo.domain.gemini.GeminiInterface;
import woozlabs.echo.domain.gemini.dto.GeminiRequest;
import woozlabs.echo.domain.gemini.dto.GeminiResponse;
import woozlabs.echo.domain.gmail.dto.thread.GmailThreadGetMessages;
import woozlabs.echo.domain.gmail.dto.thread.GmailThreadGetPart;
import woozlabs.echo.domain.gmail.dto.thread.GmailThreadGetResponse;

import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GeminiService {

    public static final String GEMINI_PRO = "gemini-pro";
    public static final String GEMINI_ULTIMATE = "gemini-ultimate";
    public static final String GEMINI_PRO_VISION = "gemini-pro-vision";

    private final GeminiInterface geminiInterface;

    private GeminiResponse getCompletion(GeminiRequest request) {
        return geminiInterface.getCompletion(GEMINI_PRO, request);
    }

    public String getCompletion(String text) {
        GeminiRequest geminiRequest = new GeminiRequest(text);
        GeminiResponse response = getCompletion(geminiRequest);

        return response.getCandidates()
                .stream()
                .findFirst().flatMap(candidate -> candidate.getContent().getParts()
                        .stream()
                        .findFirst()
                        .map(GeminiResponse.TextPart::getText))
                .orElse(null);
    }

    public String getThreadSummaryPrompt(String threadContent) {
        return String.format("""
            You are an AI assistant specializing in creating concise, natural-sounding email summaries. Your task is to provide a brief summary of the given Gmail thread, focusing on the most important points.
    
            Guidelines:
            1. Summarize the thread in 3-5 sentences, regardless of the thread's length.
            2. Focus on the most critical information, action items, or decisions.
            3. Use clear, direct language without unnecessary details.
            4. Present the summary as a cohesive paragraph, not bullet points.
            5. Exclude all pleasantries, greetings, and signatures.
            6. Do not mention names or email addresses unless absolutely crucial to the context.
            7. Use a natural, flowing style that's easy to read quickly.
            8. If the thread is mostly irrelevant, state this briefly in 1-2 sentences.
    
            Analyze and summarize the following Gmail thread content:
    
            %s
    
            Concise, natural summary (3-5 sentences):
            """, threadContent
        );
    }

    public String summarizeGmailThread(GmailThreadGetResponse gmailThread) {
        StringBuilder threadContent = new StringBuilder();
        for (GmailThreadGetMessages message : gmailThread.getMessages()) {
            extractContentFromParts(message.getPayload().getParts(), threadContent);
        }

        System.out.println("threadContent = " + threadContent);
        String prompt = getThreadSummaryPrompt(threadContent.toString());
        return getCompletion(prompt);
    }

    private void extractContentFromParts(List<GmailThreadGetPart> parts, StringBuilder threadContent) {
        for (GmailThreadGetPart part : parts) {
            String mimeType = part.getMimeType();
            if ("text/plain".equals(mimeType) || "text/html".equals(mimeType)) {
                processTextPart(part, threadContent, mimeType.equals("text/plain") ? "Content" : "HTML Content");
            } else if (mimeType.startsWith("multipart/")) {
                extractContentFromParts(part.getParts(), threadContent);
            }
        }
    }

    private void processTextPart(GmailThreadGetPart part, StringBuilder threadContent, String contentType) {
        String bodyData = part.getBody().getData();
        if (bodyData != null) {
            String decodedBody = new String(Base64.getUrlDecoder().decode(bodyData));
            threadContent.append(contentType).append(": ").append(decodedBody).append("\n\n");
        }
    }
}
