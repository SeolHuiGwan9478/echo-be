package woozlabs.echo.domain.chatGPT.prompt;

public class EmailTemplateSuggestionPrompt {
    private static String EMAIL_TEMPLATE_SUGGESTION_PROMPT = """
    You are an AI assistant that analyzes email content and suggests appropriate response template ideas.
    Based on the given email content, please provide 3 suggestions for response template topics.
    
    Email content:
    %s
    
    Analyze the above email content and suggest 3 response template ideas in the following JSON format:
    {"suggestions": [
      "[Template suggestion 1]",
      "[Template suggestion 2]",
      "[Template suggestion 3]"
    ]}
    
    The suggestions should meet the following criteria:
    - Be directly related to the email's subject
    - Be specific and relevant to the email content
    - Always be phrased in the format equivalent to "Shall we generate a template about [topic]?" in the email's language
    - Cover different aspects or possible interpretations of the email content
    
    Important: Respond in the same language as the email content. Ensure that the entire suggestion, including the phrase equivalent to "Shall we generate a template about", is in the same language as the input email.
    
    Note: Provide only the JSON object with the suggestions. Do not include any other explanations or additional information.
    """;

    public static String getPrompt(String messageContent) {
        return String.format(EMAIL_TEMPLATE_SUGGESTION_PROMPT, messageContent);
    }
}