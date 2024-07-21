package woozlabs.echo.domain.gemini.prompt;

public class ThreadKeypointPrompt {

    private static final String EXTRACT_KEYPOINT_GUIDELINES = """
            Extract key points from the following text. The output should:
            1. Be a list of concise, bullet-pointed statements
            2. Capture the main ideas and essential information
            3. Use clear and simple language
            4. Start each point with a bullet point (•)
            5. Limit to a maximum of 5-7 key points
            6. Respond in the same language as the input text
            7. Do not use any other punctuation or symbols at the start of each point
        
            Text to analyze:
            %s
       
            Key points:
            """;

    public static String getPrompt(String text) {
        return String.format(EXTRACT_KEYPOINT_GUIDELINES, text);
    }
}
