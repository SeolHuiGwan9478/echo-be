package woozlabs.echo.domain.chatGPT.prompt;

import java.time.LocalDateTime;
import java.util.List;

public class ScheduleEmailTemplatePrompt {
    private static final String SCHEDULE_EMAIL_TEMPLATE_PROMPT = """
        Analyze the following email content and create a response email template from the recipient's perspective to schedule a meeting. 
        The recipient can provide available dates and times up to 2 weeks from today, excluding the unavailable dates and times provided. 
        Consider the following scenarios:

        1. If the sender provides specific options:
        - Check that the date and time suggested by the other party do not overlap with the recipient's unavailable dates and times provided below.
        - If there is no overlap, choose one of the suggested dates and times. Create a template confirming the date and time, and asking if it works for the sender.
        - If there is overlap, create a template informing the sender that none of the provided dates and times work, and politely ask if other options might be possible.
        - If none of the proposed dates and times are available, suggest that the meeting is impossible during the provided time slots by mentioning the time zones in the following format: 2024-08-23T13:30:00.000+09:00 ~ 2024-08-23T14:30:00.000+09:00.

        2. If the sender mentions relative date phrases like "this week", "next week", or similar:
        - Interpret these based on the current date provided below.
        - Select one appropriate date and time except for the recipient's unavailable dates and times provided.
        - Create a template proposing that date and time, and end by asking if that schedule is acceptable.
        - If none of the proposed dates and times are available, suggest that the meeting is impossible during the provided time slots by mentioning the time zones in the following format: 2024-08-23T13:30:00.000+09:00 ~ 2024-08-23T14:30:00.000+09:00, and politely ask if other options might be possible.

        3. If the sender is asking for dates and times without specific options:
        - Create a template that prioritizes weekends and daytime hours from the recipient's available dates and times (excluding unavailable dates and times).

        4. If the recipient's unavailable dates and times include the sender's proposed dates and times:
        - Politely inform the sender that those dates and times are unavailable.
        - Suggest alternative dates and times from the recipient's available options.
        - If no suitable alternatives exist, ask if the sender has other options in mind.

        Include the following elements in the template:
        - A polite greeting
        - Email body (proposing a date and time or requesting a change)
        - A thank you note and closing

        Return the template as a JSON string with the following structure:
        {
          "template": String,
          "isSchedule": boolean
        }
        - Please remove the back tick.

        The "isSchedule" field should be true if the email is about a schedule or event, and false otherwise.
        If the "isSchedule" field is false, the "template" field should be an empty string.

        Please make it 150 characters maximum.

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
        """;
    public static String getPrompt(String messageContent, List<String> unAvailableDates) {
        return String.format(SCHEDULE_EMAIL_TEMPLATE_PROMPT, messageContent, unAvailableDates.toString(), LocalDateTime.now());
    }
}
