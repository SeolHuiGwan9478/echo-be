package woozlabs.echo.domain.gemini.prompt;

public class VerificationMailPrompt {

    private static final String VERIFICATION_MAIL_ANALYSIS = """
    You are a professional email analyzer. Your job is to determine if an email is an account verification email and extract relevant information.

    Definition:
    An generic verification email includes emails sent for purposes such as email verification, password reset, account activation, or any email requiring the user to verify their identity or actions.

    Instructions:
    1. Carefully analyze the following email content, paying close attention to the context.
    2. If it's clearly an account verification email in HTML format:
       - If there's a verification URL, respond with: <element=LINK, id=ID_ATTRIBUTE>
       - If there's a verification code (which will be a combination of numbers or letters, never in URL format), respond with: <element=CODE, id=ID_ATTRIBUTE>
       - Ensure that ID_ATTRIBUTE is the four-digit number id attribute of the element containing the verification URL or code.
    3. If it's not a verification email, or if you're unsure about the context, respond with: "false"
    4. If the content is completely unrelated or you cannot make a determination, respond with: "unknown"

    Be aware that verification codes are always combinations of numbers or letters and are never in URL format. If you encounter any ambiguity in the context, err on the side of caution and respond with "false".

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
