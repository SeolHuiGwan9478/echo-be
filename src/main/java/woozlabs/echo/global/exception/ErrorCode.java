package woozlabs.echo.global.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // Global HttpStatus
    BAD_REQUEST(400, "Invalid Request"),
    UNAUTHORIZED_REQUEST(401, "Unauthorized"),
    FORBIDDEN_ACCESS(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),
    METHOD_NOT_ALLOWED(405, "Not Allowed method"),
    INTERNAL_SERVER_ERROR(500, "Server Error"),
    OBJECT_MAPPER_JSON_PARSING_ERROR_MESSAGE(500, "ObjectMapper Json Parsing Error"),

    // firebase
    NOT_FOUND_FIREBASE_SERVICE_ACCOUNT_KEY(404, "Not found Firebase SDK file"),
    FIREBASE_CLOUD_MESSAGING_SEND_ERR(500, "FCM(Firebase Cloud Messaging) Send Error"),
    NOT_FOUND_FIREBASE_CLOUD_MESSAGING_TOKEN_ERR(404, "Not Found FCM Token"),
    FAILED_TO_CREATE_CUSTOM_TOKEN(500, "Failed to create custom token"),
    FAILED_TO_SET_CUSTOM_CLAIMS(500, "Failed to set custom claims"),
    DUPLICATE_FCM_TOKEN_ERR(400,"Is already exist this token"),
    EXCEED_FCM_TOKEN_SIZE_ERR(400, "Exceed maximum token size"),

    // account
    NOT_FOUND_ACCOUNT_ERROR_MESSAGE(404, "Not found: Account"),
    NOT_VERIFY_ID_TOKEN(401, "ID token is incorrect"),
    NOT_FOUND_MEMBER(404, "Member not found"),
    NOT_FOUND_VERIFY_TOKEN(404, "Not found verifyToken"),
    FAILED_TO_FETCH_GOOGLE_USER_INFO(500, "Failed to fetch Google user info"),
    FAILED_TO_FETCH_GOOGLE_USER_INFO_UTILS(500, "Failed to fetch Google user info (GoogleOAuthUtils)"),
    FAILED_TO_REDIRECT_GOOGLE_USER_INFO(500, "Failed to redirect Google user info"),
    FAILED_TO_REFRESH_GOOGLE_TOKEN(500, "Failed to refresh google token"),
    FAILED_TO_FETCH_GOOGLE_TOKENS(500, "Failed to get Google access tokens"),
    INVALID_FIELD_TYPE_ERROR_MESSAGE(400, "Invalid Account Field Type"),
    INVALID_SESSION(400, "Session is empty or invalid"),

    // token
    NOT_FOUND_ACCESS_TOKEN(404, "Not found: Access Token"),
    NOT_FOUND_REFRESH_TOKEN(404, "Not found: Refresh Token"),
    FAILED_TO_VERIFY_ID_TOKEN(500, "failed to verify Id Token"),

    // email
    REQUEST_GMAIL_USER_THREADS_GET_API_ERROR_MESSAGE(500, "Failed to get gmail threads api"),
    REQUEST_GMAIL_USER_MESSAGES_SEND_API_ERROR_MESSAGE(500, "Failed to send gmail messages api"),
    REQUEST_GMAIL_USER_DRAFTS_SEND_API_ERROR_MESSAGE(500, "Failed to send draft messages api"),
    FAILED_TO_GET_GMAIL_CONNECTION_REQUEST(500, "Failed to get connection gmail api"),
    FAILED_TO_CHANGE_DATE_FORMAT(500, "Failed to change date format"),
    INVALID_ACCESS_TOKEN(401, "Invalid Access Token"),
    TOO_MANY_REQUESTS(429, "Too many requests"),
    INVALID_NEXT_PAGE_TOKEN(400, "Invalid next page token"),
    BILLING_ERROR_MESSAGE(402, "Payment Required"),

    // calendar
    GOOGLE_CALENDAR_SECURITY_ERROR(500, "Security error while fetching Google Calendar events"),
    FAILED_TO_FETCH_GOOGLE_CALENDAR(500, "Failed to get Google Calendar Events"),
    FAILED_TO_POST_GOOGLE_CALENDAR(500, "Failed to post Google Calendar Events"),
    FAILED_TO_UPDATE_GOOGLE_CALENDAR(500, "Failed to update Google Calendar Events"),
    FAILED_TO_DELETE_GOOGLE_CALENDAR(500, "Failed to delete Google Calendar Events"),

    // gemini
    FAILED_TO_GEMINI_COMPLETION(500, "Error while getting completion from Gemini"),
    FAILED_TO_SUMMARIZE_GEMINI(500, "Error summarizing Gmail thread from Gemini"),
    FAILED_TO_CHANGE_TONE(500, "Error changing tone from Gemini"),
    FAILED_TO_PROOFREAD(500, "Error proofread from Gemini"),
    FAILED_TO_SUMMARIZE_TEXT(500, "Error summarizing text from Gemini"),
    FAILED_TO_EXTRACT_KEYPOINT(500, "Error extracting keypoint from Gemini"),
    FAILED_TO_PARSE_GEMINI_RESPONSE(500, "Error parsing GEMINI response"),

    // chatGPT
    FAILED_TO_CHATGPT_COMPLETION(500, "Error while getting completion from chatGPT"),

    // Email Template
    FAILED_TO_FETCHING_EMAIL_TEMPLATE(500, "Error occurred while fetching email templates for user"),
    FAILED_TO_CREATE_EMAIL_TEMPLATE(500, "Error occurred while creating email templates for user"),
    FAILED_TO_UPDATE_EMAIL_TEMPLATE(500, "Error occurred while updating email templates for user"),
    FAILED_TO_DELETE_EMAIL_TEMPLATE(500, "Error occurred while deleting email templates for user"),
    NOT_FOUND_EMAIL_TEMPLATE(404, "Not Found: EmailTemplate"),
    UNAUTHORIZED_ACCESS_TO_TEMPLATE(401, "Unauthorized access to template"),

    // Side Bar
    NOT_FOUND_SIDE_BAR_CONFIG(404, "Not Found: SideBar Config"),
    INVALID_ACCOUNT_UID(400, "Is not linked to the primary account's super account"),

    // Organization
    NOT_FOUND_ORGANIZATION(404, "Not Found: Organization"),
    NOT_FOUND_CONTACT_GROUP(404, "Not Found: Contact Group"),

    // team
    NOT_FOUND_TEAM(404, "Not Found: Team"),
    FAILED_TO_INVITATION_MAIL(500, "Failed to send email"),
    NOT_FOUND_INVITATION_TOKEN(404, "Not Found: Invitation Token"),
    INVITATION_EXPIRED(400, "The team invitation approval deadline has expired"),
    NOT_FOUND_TEAM_MEMBER(404, "The Account can't be found in the team"),
    NOT_FOUND_SHARED_EMAIL(404, "Not Found: Shared Email"),
    UNAUTHORIZED_ACCESS(401, "You can't access this feature with this privilege"),

    // verification
    KEYWORD_IO_EXCEPTION(500, "I/O Exception finding verification keywords"),
    EXTRACT_VERIFICATION_LINK_ERR(500, "Extract Verification Link Error"),
    EXTRACT_VERIFICATION_CODE_ERR(500, "Extract Verification Code Error"),

    // pub/sub
    CLOUD_PUB_SUB_WATCH_ERR(400, "Failed to watch cloud pub/sub"),
    CLOUD_PUB_SUB_STOP_ERR(500, "Failed to stop cloud pub/sub"),
    NOT_FOUND_PUB_SUB_HISTORY_ERR(404, "Not Found: PubSubHistory");

    private final int status;
    private final String message;

    ErrorCode(final int status, final String message) {
        this.status = status;
        this.message = message;
    }
}