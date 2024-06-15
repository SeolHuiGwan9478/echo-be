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

    public static ResponseEntity<CustomErrorResponse> toResponseEntity(ErrorCode e) {
        return ResponseEntity
                .status(e.getStatus())
                .body(CustomErrorResponse.builder()
                    .status(e.getStatus())
                    .name(e.name())
                    .message(e.getMessage())
                    .build());
    }
}
