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

    // member
    NOT_FOUND_MEMBER_ERROR_MESSAGE(404, "Not found: Member"),
    NOT_VERIFY_ID_TOKEN(401, "ID token is incorrect"),
    NOT_FOUND_SUPER_ACCOUNT(404, "Super account not found"),
    NOT_FOUND_VERIFY_TOKEN(404, "Not found verifyToken"),
    FAILED_TO_FETCH_GOOGLE_USER_INFO(500, "Failed to fetch Google user info"),
    FAILED_TO_FETCH_GOOGLE_USER_INFO_UTILS(500, "Failed to fetch Google user info (GoogleOAuthUtils)"),
    FAILED_TO_REDIRECT_GOOGLE_USER_INFO(500, "Failed to redirect Google user info"),
    FAILED_TO_REFRESH_GOOGLE_TOKEN(500, "Failed to refresh google token"),
    FAILED_TO_FETCH_GOOGLE_TOKENS(500, "Failed to get Google access tokens"),

    // token
    NOT_FOUND_ACCESS_TOKEN(404, "Not found: Access Token"),
    NOT_FOUND_REFRESH_TOKEN(404, "Not found: Refresh Token"),
    FAILED_TO_CREATE_CUSTOM_TOKEN(500, "Failed to create custom token"),
    FAILED_TO_VERIFY_ID_TOKEN(500, "failed to verify Id Token"),

    // email
    REQUEST_GMAIL_USER_THREADS_GET_API_ERROR_MESSAGE(500, "Failed to get gmail threads api"),
    REQUEST_GMAIL_USER_MESSAGES_SEND_API_ERROR_MESSAGE(500, "Failed to send gmail messages api"),
    REQUEST_GMAIL_USER_DRAFTS_SEND_API_ERROR_MESSAGE(500, "Failed to send draft messages api"),
    FAILED_TO_GET_GMAIL_CONNECTION_REQUEST(500, "Failed to get connection gmail api"),
    FAILED_TO_CHANGE_DATE_FORMAT(500, "Failed to change date format"),

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
    FAILED_TO_CHECK_GRAMMAR(500, "Error checking grammar from Gemini"),
    FAILED_TO_SUMMARIZE_TEXT(500, "Error summarizing text from Gemini"),
    FAILED_TO_EXTRACT_KEYPOINT(500, "Error extracting keypoint from Gemini"),

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

    // verification
    KEYWORD_IO_EXCEPTION(500, "I/O Exception finding verification keywords");

    private final int status;
    private final String message;

    ErrorCode(final int status, final String message) {
        this.status = status;
        this.message = message;
    }
}