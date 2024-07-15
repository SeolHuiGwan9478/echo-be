package woozlabs.echo.domain.gemini.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import woozlabs.echo.domain.gemini.GeminiInterface;
import woozlabs.echo.domain.gemini.dto.GeminiRequest;
import woozlabs.echo.domain.gemini.dto.GeminiResponse;
import woozlabs.echo.domain.gmail.dto.thread.GmailThreadGetMessages;
import woozlabs.echo.domain.gmail.dto.thread.GmailThreadGetPart;
import woozlabs.echo.domain.gmail.dto.thread.GmailThreadGetResponse;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;

import java.util.Base64;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {

    public static final String GEMINI_PRO = "gemini-pro";
    public static final String GEMINI_ULTIMATE = "gemini-ultimate";
    public static final String GEMINI_PRO_VISION = "gemini-pro-vision";

    private final GeminiInterface geminiInterface;

    private GeminiResponse getCompletion(GeminiRequest request) {
        try {
            return geminiInterface.getCompletion(GEMINI_PRO, request);
        } catch (Exception e) {
            log.error("Error while getting completion from Gemini", e);
            throw new CustomErrorException(ErrorCode.FAILED_TO_GEMINI_COMPLETION, e.getMessage());
        }
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
            1. Summarize in 1-2 sentences, regardless of thread length.
            2. Focus on the most critical information, action items, or decisions.
            3. Omit all pleasantries, greetings, signatures, and unnecessary details.
            4. Present the summary as a cohesive paragraph, not bullet points.
            5. Do not mention names or email addresses unless absolutely crucial to the context.
            6. Use extremely concise and direct language.
            7. If the thread is mostly irrelevant, state this briefly in 1-2 sentences.
    
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
