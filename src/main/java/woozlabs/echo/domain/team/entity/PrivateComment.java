package woozlabs.echo.domain.team.entity;

import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "private_comments")
public class PrivateComment {

    @Id
    private String id;
    private String sharedEmailId;
    private String authorId; // 누가 작성했는지 알기 위한 Id (임시)
    private Map<String, String> encryptedContents; // userId, Contents
    private LocalDateTime createdAt;
}
