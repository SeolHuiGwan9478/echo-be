package woozlabs.echo.global.constant;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class GlobalConstant {
    // End Points
    public static final String FE_HOST_ADDRESS = "http://127.0.0.1:3000";
    // Basic Char
    public static final String SPACE_CHAR = " ";
    // Gmail global
    public static final String USER_ID = "me";
    // Gmail threads
    public static final String THREAD_PAYLOAD_HEADER_SUBJECT_KEY = "Subject";
    public static final String THREAD_PAYLOAD_HEADER_FROM_KEY = "From";
    public static final String THREAD_PAYLOAD_HEADER_DATE_KEY = "Date";
    public static final String THREADS_GET_METADATA_FORMAT = "metadata";
    public static final String THREADS_GET_FULL_FORMAT = "full";
    public static final Long THREADS_LIST_MAX_LENGTH = 50L;
    public static final String THREADS_LIST_Q = "is:inbox";
    // Email Error Message
    public static final String EMAIL_ERR_MSG_KEY = "EmailException";
    public static final String REQUEST_GMAIL_USER_MESSAGES_GET_API_ERR_MSG = "Internal Server Error: Request gmail messages get one api";
}