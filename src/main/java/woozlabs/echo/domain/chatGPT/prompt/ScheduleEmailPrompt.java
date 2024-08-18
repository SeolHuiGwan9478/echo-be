package woozlabs.echo.domain.chatGPT.prompt;

public class ScheduleEmailPrompt {
    private static final String SCHEDULE_MAIL_ANALYSIS = """
            Email Content:
            [Insert the actual email content here]
                        
            Information to Extract:
            1. Date (dt): All dates mentioned in the email (format as "yyyy-MM-dd HH:mm:ss")
            2. Location (loc): All locations mentioned in the email
            3. Person (per): All names of people mentioned in the email
            4. Is Schedule (isSchedule): Determine if the email is about a schedule or event
                        
            Please extract the dates (dt), locations (loc), and persons (per) from the above email content. Format all dates as "yyyy-MM-dd HH:mm:ss" with the following rules:
            - If the year is not specified, use the current year %s
                        
            Also, determine if the email is about a schedule or event.
                        
            Return the extracted information as a JSON string with the following structure:
            {
              "dt": ["yyyy-MM-dd HH:mm:ss", ...],
              "loc": ["location1", "location2", ...],
              "per": ["person1", "person2", ...],
              "isSchedule": boolean
            }
            - Please remove the back tick.
                        
            If no information is found for a category, please use an empty array [].
            The "isSchedule" field should be true if the email is about a schedule or event, and false otherwise.
                
            Email content to analyze:
            ===
            %s
            ===
            """;

    public static String getPrompt(String threadContent, String currentDateTime) {
        return String.format(SCHEDULE_MAIL_ANALYSIS, currentDateTime, threadContent);
    }
}
