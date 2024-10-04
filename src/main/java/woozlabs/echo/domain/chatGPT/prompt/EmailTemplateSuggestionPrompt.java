package woozlabs.echo.domain.chatGPT.prompt;

public class EmailTemplateSuggestionPrompt {
    private static String EMAIL_TEMPLATE_SUGGESTION_PROMPT = """
    You are an AI assistant that analyzes email content and suggests response topics.
    Based on the given email content, provide 3 concise topic suggestions for the response, including at least one Yes/No question when applicable.
    
    Email content:
    %s
    
    Analyze the above email content and suggest 3 response topics in the following JSON format:
    {"suggestions": [
      "[Topic suggestion 1]",
      "[Topic suggestion 2]",
      "[Topic suggestion 3 (Yes/No question when applicable)]"
    ]}
    
    The suggestions should:
    - Be directly related to the email's subject
    - Be specific and relevant to the email content
    - Be concise, focusing on the core topic
    - Cover different aspects or possible interpretations of the email content
    - Include at least one Yes/No question when applicable, preferably as the third suggestion
    
    Important: Respond in the same language as the email content. Ensure that the suggestions are in the same language as the input email.
    
    Note: Provide only the JSON object with the suggestions. Do not include any other explanations or additional information.
    """;

    public static String getPrompt(String messageContent) {
        return String.format(EMAIL_TEMPLATE_SUGGESTION_PROMPT, messageContent);
    }
}