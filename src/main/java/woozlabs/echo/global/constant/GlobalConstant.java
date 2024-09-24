package woozlabs.echo.global.constant;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class GlobalConstant {
    // Auth
    public static final String AUTH_UNAUTHORIZED_ERR_MSG = "인증되지 않은 사용자입니다.";
    public static final String AUTH_SIGN_IN_DOMAIN = "https://echo-homepage.woozlabs.com/sign-in";
    public static final String AUTH_ADD_ACCOUNT_DOMAIN = "https://echo-homepage.woozlabs.com/add-account";
    // End Points
    public static final String ECHO_NEXT_APP_DOMAIN = "https://echo-homepage.vercel.app";
    // Basic Char
    public static final String EMPTY_CHAR = "";
    // Gmail global
    public static final String USER_ID = "me";
    // Gmail messages
    public static final String MESSAGE_PAYLOAD_HEADER_SUBJECT_KEY = "SUBJECT";
    public static final String MESSAGE_PAYLOAD_HEADER_FROM_KEY = "FROM";
    public static final String MESSAGE_PAYLOAD_HEADER_DATE_KEY = "DATE";
    public static final String MESSAGE_PAYLOAD_HEADER_CC_KEY = "CC";
    public static final String MESSAGE_PAYLOAD_HEADER_BCC_KEY = "BCC";
    public static final String MESSAGE_PAYLOAD_HEADER_TO_KEY = "TO";
    public static final String DELETED_MESSAGE_ALERT_MSG = "Not Found: Message(Reason: Deleted Message)";
    // Gmail threads
    public static final String THREAD_PAYLOAD_HEADER_SUBJECT_KEY = "SUBJECT";
    public static final String THREAD_PAYLOAD_HEADER_FROM_KEY = "FROM";
    public static final String THREAD_PAYLOAD_HEADER_CC_KEY = "CC";
    public static final String THREAD_PAYLOAD_HEADER_BCC_KEY = "BCC";
    public static final String THREAD_PAYLOAD_HEADER_TO_KEY = "TO";
    public static final String THREAD_PAYLOAD_HEADER_CONTENT_ID_KEY = "CONTENT-ID";
    public static final String THREADS_GET_FULL_FORMAT = "full";
    public static final Long THREADS_LIST_MAX_LENGTH = 50L;

    // Gmail drafts
    public static final String DRAFTS_GET_FULL_FORMAT = "full";
    public static final String DRAFT_PAYLOAD_HEADER_FROM_KEY = "FROM";
    public static final String DRAFT_PAYLOAD_HEADER_DATE_KEY = "DATE";

    // Gmail History
    public static final String HISTORY_INBOX_LABEL = "INBOX";

    // Email Error Message
    public static final String EMAIL_ERR_MSG_KEY = "EmailException";
    public static final String REQUEST_GMAIL_USER_MESSAGES_GET_API_ERR_MSG = "Internal Server Error: Request gmail messages get one api";
    // firebase
    public static final String FIREBASE_UID_KEY = "uid";

    // Date Format
    public static final String TIMEZONE_PATTERN_2 = "[+-]\\d{4}";

    // Verification
    public static final String VERIFICATION_LABEL = "Echo/Verification";
}