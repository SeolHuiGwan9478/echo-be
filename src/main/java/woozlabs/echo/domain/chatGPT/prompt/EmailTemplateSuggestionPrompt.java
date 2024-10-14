package woozlabs.echo.domain.chatGPT.prompt;

public class EmailTemplateSuggestionPrompt {
    private static String EMAIL_TEMPLATE_SUGGESTION_PROMPT = """
    You are an AI assistant tasked with analyzing email content and suggesting appropriate response keywords. Please follow these instructions based on the given email content:
    
    1. Carefully read and analyze the following email content:
       "%s"
    
    2. Determine the language of the email content and ensure that your response keywords are in the same language.
    
    3. Determine if a response is needed:
       - If no response is required (e.g., simple information delivery), set isNonConversational to true.
       - If a response is needed, set isNonConversational to false.
    
    4. When a response is needed (isNonConversational is false):
       - Generate 1-3 possible response keywords in the same language as the email content.
       - Each keyword should be concise and clear.
       - For Yes/No questions, include appropriate affirmative and negative responses in the email's language.
    
    5. Return the result in the following JSON format:
       {
         "isNonConversational": boolean,
         "suggestions": [string] (only when isNonConversational is false)
       }
    
    IMPORTANT: Strictly adhere to the specified JSON output format. Do not include any additional text, explanations, or formatting outside of this JSON structure. The output must be valid JSON that can be directly parsed by a JSON parser.
    
    Examples:
    Input: "What should we have for dinner tomorrow?"
    Output:
    {
      "isNonConversational": false,
      "suggestions": [
        "Suggest everyone's okay with anything",
        "Mention having prior plans",
        "Express desire for chicken"
      ]
    }
    
    Input: "明日の夕食は何にしましょうか？"
    Output:
    {
      "isNonConversational": false,
      "suggestions": [
        "何でも大丈夫だと答える",
        "予定があると伝える",
        "チキンが食べたいと答える"
      ]
    }
    
    Input: "Here's the meeting schedule for next week: Monday at 10 AM, Tuesday at 2 PM"
    Output:
    {
      "isNonConversational": true
    }
    
    Now, please analyze the given email content according to these guidelines, matching the language of the email, and provide an appropriate JSON response. Remember to strictly follow the specified output format.
    """;

    public static String getPrompt(String messageContent) {
        return String.format(EMAIL_TEMPLATE_SUGGESTION_PROMPT, messageContent);
    }
}