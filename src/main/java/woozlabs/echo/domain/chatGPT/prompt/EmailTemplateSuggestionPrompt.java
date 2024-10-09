package woozlabs.echo.domain.chatGPT.prompt;

public class EmailTemplateSuggestionPrompt {
    private static String EMAIL_TEMPLATE_SUGGESTION_PROMPT = """
    You are an AI assistant that analyzes email content, detects if it's a non-conversational email, and suggests context-appropriate response topics if applicable. 
    First, determine if the email is non-conversational (promotional, containing authentication codes/links, or simple notifications). 
    Then, only if it is conversational, provide 1-3 concise topic suggestions for the response, such as questions related to "salary negotiation", "company benefits", or "scheduling an interview". 
    If no relevant suggestions apply, it is not necessary to provide all three suggestions.

    Email content:
    %s

    Analyze the above email content and respond in the following JSON format:
    {
      "isNonConversational": boolean,
      "suggestions": [
        "[Topic suggestion 1]",
        "[Topic suggestion 2 (if applicable)]",
        "[Topic suggestion 3 (if applicable)]"
      ]
    }

    Guidelines:
    1. Set "isNonConversational" to true if the email is:
       - Promotional or advertising in nature
       - Contains authentication codes or links
       - Is a simple notification or alert
       - Any other type of email that doesn't require a conversational response
    2. If "isNonConversational" is true, provide an empty list for "suggestions".
    3. If "isNonConversational" is false, provide 1-3 topic suggestions, each limited to 20 characters.
    4. IMPORTANT: Ensure that each suggestion represents a potential "response" to the email. 
       The suggestions should be highly relevant to the email's context and content, addressing key points or questions raised in the email that require a reply.
       For example, suggestions may include: "Ask about salary", "Discuss company benefits", or "Schedule an interview".
    5. For Yes/No questions, frame them as "Please answer 'Yes'" or "Please answer 'No'".
    6. Make suggestions specific, concise, and cover different aspects of the content that need to be addressed in a reply.
    7. CRITICAL: If no suitable suggestions are found, do not feel obligated to provide 3 suggestions; fewer is acceptable.
       Your entire response, including all text within the JSON object, MUST be in the exact same language as the provided email content. Do not translate any part of your response to a different language.

    Note: Provide only the JSON object as specified. Do not include any other explanations or additional information.
    """;

    public static String getPrompt(String messageContent) {
        return String.format(EMAIL_TEMPLATE_SUGGESTION_PROMPT, messageContent);
    }
}