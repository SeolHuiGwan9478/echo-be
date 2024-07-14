package woozlabs.echo.global.exception;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.ResponseEntity;

@Getter
@Builder
public class CustomErrorResponse {

    private int status;
    private String name;
    private String message;
    private String detailMessage;

    public static ResponseEntity<CustomErrorResponse> toResponseEntity(ErrorCode e, String detailMessage) {
        return ResponseEntity
                .status(e.getStatus())
                .body(CustomErrorResponse.builder()
                        .status(e.getStatus())
                        .name(e.name())
                        .message(e.getMessage())
                        .detailMessage(detailMessage)
                        .build());
    }
}
