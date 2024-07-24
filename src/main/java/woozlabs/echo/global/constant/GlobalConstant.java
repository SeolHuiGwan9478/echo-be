package woozlabs.echo.global.constant;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class GlobalConstant {
    // Auth
    public static final String AUTH_UNAUTHORIZED_ERR_MSG = "인증되지 않은 사용자입니다.";
    public static final String AUTH_SIGN_IN_DOMAIN = "https://echo-homepage.vercel.app/sign-in";
    // End Points
    public static final String FE_HOST_ADDRESS = "http://127.0.0.1:3000";
    // Basic Char
    public static final String SPACE_CHAR = " ";
    public static final String EMPTY_CHAR = "";
    // Gmail global
    public static final String USER_ID = "me";
    // Gmail threads
    public static final String THREAD_PAYLOAD_HEADER_SUBJECT_KEY = "Subject";
    public static final String THREAD_PAYLOAD_HEADER_FROM_KEY = "From";
    public static final String THREAD_PAYLOAD_HEADER_DATE_KEY = "Date";
    public static final String THREAD_PAYLOAD_HEADER_CC_KEY = "Cc";
    public static final String THREAD_PAYLOAD_HEADER_BCC_KEY = "Bcc";
    public static final String THREAD_PAYLOAD_HEADER_TO_KEY = "To";
    public static final String THREADS_GET_METADATA_FORMAT = "metadata";
    public static final String THREADS_GET_FULL_FORMAT = "full";
    public static final Long THREADS_LIST_MAX_LENGTH = 50L;
    public static final String THREADS_LIST_Q = "is:inbox";

    // Gmail drafts
    public static final String DRAFTS_GET_FULL_FORMAT = "full";
    public static final String DRAFT_PAYLOAD_HEADER_SUBJECT_KEY = "Subject";
    public static final String DRAFT_PAYLOAD_HEADER_FROM_KEY = "From";
    public static final String DRAFT_PAYLOAD_HEADER_DATE_KEY = "Date";

    // Email Error Message
    public static final String EMAIL_ERR_MSG_KEY = "EmailException";
    public static final String REQUEST_GMAIL_USER_MESSAGES_GET_API_ERR_MSG = "Internal Server Error: Request gmail messages get one api";
    // firebase
    public static final String FIREBASE_UID_KEY = "uid";

    // Date Format
    public static final String DATE_TIMEZONE_PATTERN = "^(?<date>\\w{3}, \\d{2} \\w{3} \\d{4} \\d{2}:\\d{2}:\\d{2})\\s(?<timezone>.*)$";
    public static final String EXTRA_TIMEZONE_PATTERN = "[+-]\\d{4} \\(\\w+\\)";
    public static final String INPUT_GMAIL_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss";
    public static final String GMT = "GMT";
}