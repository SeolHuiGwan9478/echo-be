package woozlabs.echo.domain.team.entity;

import jakarta.persistence.Id;
import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Document(collection = "private_comments")
public class PrivateComment {

    @Id
    private String id;

    private String sharedEmailId;  // MongoDB의 SharedEmail 문서 ID 참조
    private String memberId;  // MySQL의 Member 엔티티 ID 참조
    private String content;
    private LocalDateTime createdAt;
}
