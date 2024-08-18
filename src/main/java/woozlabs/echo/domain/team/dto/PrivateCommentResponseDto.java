package woozlabs.echo.domain.team.dto;

import lombok.Getter;
import woozlabs.echo.domain.team.entity.PrivateComment;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
public class PrivateCommentResponseDto {

    private String id;
    private String sharedEmailId;
    private String authorId;
    private Map<String, String> encryptedContents;
    private LocalDateTime createdAt;

    public PrivateCommentResponseDto(PrivateComment comment) {
        this.id = comment.getId();
        this.sharedEmailId = comment.getSharedEmailId();
        this.authorId = comment.getAuthorId();
        this.encryptedContents = comment.getEncryptedContents();
        this.createdAt = comment.getCreatedAt();
    }
}
