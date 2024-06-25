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

    // member
    NOT_FOUND_MEMBER_ERROR_MESSAGE(404, "Not found: Member"),
    NOT_VERIFY_ID_TOKEN(401, "ID token is incorrect"),
    NOT_FOUND_SUPER_ACCOUNT(404, "Super account not found"),
    NOT_FOUND_VERIFY_TOKEN(404, "Not found verifyToken"),

    // email
    REQUEST_GMAIL_USER_MESSAGES_GET_API_ERROR_MESSAGE(500, "Request gmail messages get one api");


    private final int status;
    private final String message;

    ErrorCode(final int status, final String message) {
        this.status = status;
        this.message = message;
    }
}