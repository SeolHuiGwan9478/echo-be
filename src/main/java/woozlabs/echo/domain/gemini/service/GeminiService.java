package woozlabs.echo.domain.gemini.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import woozlabs.echo.domain.gemini.GeminiInterface;
import woozlabs.echo.domain.gemini.dto.GeminiRequest;
import woozlabs.echo.domain.gemini.dto.GeminiResponse;
import woozlabs.echo.domain.gemini.dto.ProofreadResponse;
import woozlabs.echo.domain.gemini.prompt.ProofreadPrompt;
import woozlabs.echo.domain.gemini.prompt.ThreadKeypointPrompt;
import woozlabs.echo.domain.gemini.prompt.ThreadSummaryPrompt;
import woozlabs.echo.domain.gemini.prompt.VerificationMailPrompt;
import woozlabs.echo.domain.gmail.dto.thread.GmailThreadGetMessagesResponse;
import woozlabs.echo.domain.gmail.dto.thread.*;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;

import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {

    public static final String GEMINI_PRO = "gemini-pro";
    public static final String GEMINI_ULTIMATE = "gemini-ultimate";
    public static final String GEMINI_PRO_VISION = "gemini-pro-vision";

    private final GeminiInterface geminiInterface;
    private final ObjectMapper objectMapper;

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

    public String getCompletionWithParts(String contents, String parts) {
        GeminiRequest geminiRequest = new GeminiRequest(contents, parts);
        GeminiResponse response = getCompletion(geminiRequest);

        return response.getCandidates()
                .stream()
                .findFirst().flatMap(candidate -> candidate.getContent().getParts()
                        .stream()
                        .findFirst()
                        .map(GeminiResponse.TextPart::getText))
                .orElse(null);
    }

    public String summarizeGmailThread(GmailThreadGetResponse gmailThread) {
        StringBuilder threadContent = new StringBuilder();
        for (GmailThreadGetMessagesResponse message : gmailThread.getMessages()) {
            extractContentFromPayload(message.getPayload(), threadContent);
        }
        String prompt = ThreadSummaryPrompt.getPrompt(threadContent.toString());
        return getCompletion(prompt);
    }

    private void extractContentFromPayload(GmailThreadGetPayload payload, StringBuilder threadContent) {
        String mimeType = payload.getMimeType();
        if ("text/plain".equals(mimeType) || "text/html".equals(mimeType)) {
            processTextPart(payload, threadContent, mimeType);
        } else if (mimeType != null && mimeType.startsWith("multipart/")) {
            if (payload.getParts() != null) {
                for (GmailThreadGetPart part : payload.getParts()) {
                    extractContentFromPart(part, threadContent);
                }
            }
        }
    }

    private void extractContentFromPart(GmailThreadGetPart part, StringBuilder threadContent) {
        if (part == null) return;

        String mimeType = part.getMimeType();
        if ("text/plain".equals(mimeType) || "text/html".equals(mimeType)) {
            processTextPart(part, threadContent, mimeType);
        } else if (mimeType != null && mimeType.startsWith("multipart/")) {
            if (part.getParts() != null) {
                for (GmailThreadGetPart subPart : part.getParts()) {
                    extractContentFromPart(subPart, threadContent);
                }
            }
        }
    }

    private void processTextPart(Object part, StringBuilder threadContent, String mimeType) {
        // part가 Payload인지 Part인지 구분해서 body(본문 데이터) 불러옴
        GmailThreadGetBody body = (part instanceof GmailThreadGetPayload)
                ? ((GmailThreadGetPayload) part).getBody()
                : ((GmailThreadGetPart) part).getBody();

        String bodyData = body.getData();
        if (bodyData != null) {
            String decodedBody = new String(Base64.getUrlDecoder().decode(bodyData));
            if ("text/html".equals(mimeType)) {
                Document doc = Jsoup.parse(decodedBody, "UTF-8");
                decodedBody = doc.text();
            }
            threadContent.append(decodedBody).append("\n\n");
        }
    }

    public String changeTone(String contents, String parts, String tone) {
        String prompt = String.format(
                "Given the following context:\n\n%s\n\n" +
                        "Change the tone of only this specific part to %s, maintaining its original meaning:\n\n%s\n\n" +
                        "Provide only the modified part, keeping its length similar to the original. " +
                        "Do not explain or summarize other parts of the context.",
                contents, tone, parts
        );
        return getCompletionWithParts(contents, prompt);
    }

    public ProofreadResponse proofread(String text) {
        String prompt = ProofreadPrompt.getPrompt(text);
        String response = getCompletion(prompt);

        try {
            String sanitizedResponse = response
                    .replaceAll("[`\\p{Cntrl}&&[^\r\n\t]]", "")
                    .replaceAll("\\n\\n", " ");
            return objectMapper.readValue(sanitizedResponse, ProofreadResponse.class);
        } catch (Exception e) {
            throw new CustomErrorException(ErrorCode.FAILED_TO_PARSE_GEMINI_RESPONSE, e.getMessage());
        }
    }

    public String summarize(String text) {
        String prompt = ThreadSummaryPrompt.getGmailSummarizeGuidelinesPrompt(text);
        return getCompletion(prompt);
    }

    public String keypoint(String text) {
        String prompt = ThreadKeypointPrompt.getPrompt(text);
        return getCompletion(prompt);
    }
}
