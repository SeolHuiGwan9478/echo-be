package woozlabs.echo.domain.gemini.prompt;

public class VerificationMailPrompt {

    private static final String VERIFICATION_MAIL_ANALYSIS = """
        You are a professional email analyzer. Your job is to determine if an email is an account verification email and extract relevant information.

        Definition:
        An account verification email includes emails sent for purposes such as email verification, password reset, account activation, or any email requiring the user to verify their identity or actions.

        Instructions:
        1. Analyze the following email content.
        2. If it's an account verification email in HTML format:
           - If there's a URL, respond with: <element=ELEMENT, id=ID_ATTRIBUTE>
           - If there's a verification code, respond with: <element=ELEMENT, id=ID_ATTRIBUTE>
           - Ensure that ID_ATTRIBUTE is the four-digit number id attribute of the element containing the verification URL or code.
        3. If it's not a verification email, respond with: "false"
        4. If you're unsure, respond with: "unknown"
    
        Provide only the requested response without any additional explanation.
    
        Email content to analyze:
        ===
        %s
        ===
    
        Your analysis (respond ONLY with <element=ELEMENT, id=ID_ATTRIBUTE>, "false", or "unknown"):
        """;

    public static String getPrompt(String threadContent) {
        return String.format(VERIFICATION_MAIL_ANALYSIS, threadContent);
    }
}
