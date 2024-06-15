package woozlabs.echo.global.constant;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class GlobalConstant {
    // EndPoints
    public static final String FE_HOST_ADDRESS = "http://127.0.0.1:3000";
    // Gmail Endpoints
    public static final String GMAIL_USER_MESSAGES_LIST_API_FORMAT = "https://gmail.googleapis.com/gmail/v1/users/%s/messages?maxResults=%d";
    public static final String GMAIL_USER_MESSAGES_GET_API_FORMAT = "https://gmail.googleapis.com/gmail/v1/users/%s/messages/%s?format=minimal";
    // Global Error Message
    public static final String OBJECT_MAPPER_JSON_PARSING_ERR_MSG = "Internal Server Error: ObjectMapper Json Parsing Error";
    // Member Error Message
    public static final String NOT_FOUND_MEMBER_ERR_MSG = "Not Found: Member";
    // Email Error Message
    public static final String EMAIL_ERR_MSG_KEY = "EmailException";
    public static final String REQUEST_GMAIL_USER_MESSAGES_LIST_API_ERR_MSG = "Internal Server Error: Request gmail messages get list api";
    public static final String REQUEST_GMAIL_USER_MESSAGES_GET_API_ERR_MSG = "Internal Server Error: Request gmail messages get one api";
}