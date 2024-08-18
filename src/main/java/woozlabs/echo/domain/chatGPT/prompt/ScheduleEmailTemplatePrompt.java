package woozlabs.echo.domain.chatGPT.prompt;

import java.time.LocalDateTime;
import java.util.List;

public class ScheduleEmailTemplatePrompt {
    private static final String SCHEDULE_EMAIL_TEMPLATE_PROMPT = """
            Analyze the following email content and create a response email template from the recipient's perspective to schedule a meeting. The recipient can provide available dates up to 2 weeks from today. Consider the following two scenarios:
                        
            1. If the sender provides specific options:
            - Check if any of the dates match the recipient's available dates.
            - If there's a match, create a template selecting that date.
            - If there's no match, create a template informing the sender that none of the provided dates work, and politely ask if other dates might be possible.
            
            2. If the sender mentions relative date phrases like "this week" or "next week" or etc...:
            - Interpret these based on the current date provided below.
            - Select one of the recipient's available dates within the appropriate week.
            - Create a template proposing that date and end by asking if that day is acceptable.
            - If there's no match, create a template informing the sender that none of the provided dates work, and politely ask if other dates might be possible.
            
            3. If the sender is asking for dates without specific options:
            - Create a template that prioritizes weekends and daytime hours from the recipient's available dates.
            
            Include the following elements in the template:
            - A polite greeting
            - Email body (proposing a date or requesting a change)
            - A thank you note and closing
                        
            Return the template as a JSON string with the following structure:
            {
              "template": String,
              "isSchedule": boolean
            }
            - Please remove the back tick.
                        
            The "isSchedule" field should be true if the email is about a schedule or event, and false otherwise.
            If "isSchedule" field is false, "template" field should be empty string.
            
            Please make it 150 characters maximum.
                
            Email content to analyze:
            ===
            %s
            ===
            
            Recipient's available dates:
            ===
            %s
            ===
            
            Current date for reference:
            ===
            %s
            ===
            """;

    public static String getPrompt(String messageContent, List<String> availableDates) {
        return String.format(SCHEDULE_EMAIL_TEMPLATE_PROMPT, messageContent, availableDates.toString(), LocalDateTime.now());
    }
}
