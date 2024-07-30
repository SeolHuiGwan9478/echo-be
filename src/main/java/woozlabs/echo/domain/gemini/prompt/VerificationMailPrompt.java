package woozlabs.echo.domain.gemini.prompt;

public class VerificationMailPrompt {

    private static final String VERIFICATION_MAIL_ANALYSIS = """
          Analyze the following email content and determine if it's an account verification email.
          
          Guidelines:
          1. If it's not a verification email, respond with: <not_verification>
          2. If it is a verification email, follow these steps:
             a. Look for a verification URL. If found, respond with: <url=FULL_URL>
             b. If no URL is present, look for a verification code. If found, respond with: <code=VERIFICATION_CODE>
          3. Ensure your response contains ONLY the requested format, without any additional text.
          4. If you can't determine if it's a verification email or can't find a URL or code, respond with: <unknown>
          
          Analyze the following email content:

          %s

          Response (ONLY in the specified format):
          """;

    public static String getPrompt(String threadContent) {
        return String.format(VERIFICATION_MAIL_ANALYSIS, threadContent);
    }
}
