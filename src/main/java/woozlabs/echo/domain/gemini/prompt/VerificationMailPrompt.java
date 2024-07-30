package woozlabs.echo.domain.gemini.prompt;

public class VerificationMailPrompt {

    private static final String VERIFICATION_MAIL_ANALYSIS = """
        You are a professional email analyzer. Your job is to determine if an email is an account verification email and extract relevant information.

        Instructions:
        1. Analyze the following email content.
        2. If it's an account verification email:
           - If there's a URL, respond with: <url_title=URL>
           - If there's a verification code, respond with: <code=VERIFICATION_CODE>
        3. If it's not a verification email, respond with: "false"
        4. If you're unsure, respond with: "unknown"
    
        Provide only the requested response without any additional explanation.
    
        Email content to analyze:
        ===
        %s
        ===
    
        Your analysis (respond ONLY with <url_title=URL>, <code=VERIFICATION_CODE>, "false", or "unknown"):
        """;

    public static String getPrompt(String threadContent) {
        return String.format(VERIFICATION_MAIL_ANALYSIS, threadContent);
    }
}
