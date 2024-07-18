package woozlabs.echo.domain.gemini.prompt;

public class ThreadSummaryPrompt {

    private static final String THREAD_SUMMARY_GUIDELINES = """
            You are an AI assistant specializing in creating extremely concise email summaries.
            Your task is to provide a brief summary of the given Gmail thread, focusing on the most important points.
            
            Guidelines:
            1. Summarize in just 1 sentence with no more than 10 words.
            2. Focus on the most critical information, action items, or decisions.
            3. Omit all pleasantries, greetings, signatures, and unnecessary details.
            4. Present the summary as a single, cohesive sentence.
            5. Do not mention names or email addresses unless absolutely crucial to the context.
            6. Use extremely concise and direct language.
            7. If the thread is mostly irrelevant, state this briefly in no more than 10 words.

            Analyze and summarize the following Gmail thread content:
    
            %s
    
            Concise, natural summary (10 words or fewer):
            """;

    public static String getPrompt(String threadContent) {
        return String.format(THREAD_SUMMARY_GUIDELINES, threadContent);
    }
}
