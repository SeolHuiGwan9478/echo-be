package woozlabs.echo.domain.team.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PrivateCommentResponseDto {

    private String id;
    private String sharedEmailId;
    private String authorId;
    private String encryptedContent;
    private LocalDateTime createdAt;
}
