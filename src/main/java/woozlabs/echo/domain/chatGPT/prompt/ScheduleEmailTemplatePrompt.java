package woozlabs.echo.domain.chatGPT.prompt;

import java.time.LocalDateTime;
import java.util.List;

public class ScheduleEmailTemplatePrompt {
    private static String SCHEDULE_EMAIL_TEMPLATE_PROMPT = """
        You are an AI assistant tasked with analyzing email content and coordinating meeting schedules. Your job is to review the given email content, determine if it's about scheduling a meeting, and generate an appropriate response template.
        
        Email content to analyze:
        ===
        %s
        ===
        
        Recipient's unavailable dates and times:
        ===
        %s
        ===
        Current date and time for reference:
        ===
        %s
        ===
        
        Follow these instructions to respond:
        
        1. Determine if the email is about scheduling a meeting.
        
        2. If the email is about scheduling a meeting:
           a) Carefully check if the proposed date and time conflict with any of the unavailable dates.
           b) If there's no conflict, confirm the proposed date and time in your response.
           c) If there is a conflict or no specific date was proposed, suggest the nearest available date and time that doesn't conflict with the unavailable dates.
           d) Make sure to double-check that your suggested time does not fall within any of the unavailable time ranges.
        
        3. If the email is not about scheduling a meeting:
           a) Set "isSchedule" to false.
           b) In the "template" field, create an appropriate email reply template based on your analysis of the email content
        
        4. Output the result in JSON format with the following structure:
           {
             "isSchedule": boolean,
             "template": string
           }
           Where 'isSchedule' is true if the email is about scheduling a meeting, false otherwise.
           The 'template' should contain a polite response either confirming the proposed time or suggesting an alternative.
        
        Note: Maintain a polite and professional tone. Pay close attention to the unavailable dates and times, ensuring no conflicts. Your entire response must be a valid JSON object without any additional text or formatting.
    """;

    public static String getPrompt(String messageContent, List<String> unAvailableDates) {
        return String.format(SCHEDULE_EMAIL_TEMPLATE_PROMPT, messageContent, String.join("\n", unAvailableDates), LocalDateTime.now());
    }
}