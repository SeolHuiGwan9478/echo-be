package woozlabs.echo.domain.team.entity;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@Document(collection = "private_comments")
public class PrivateComment {

    @Id
    private String id;
    private String sharedEmailId;
    private String authorId;
    private Map<String, String> encryptedContents;
    private LocalDateTime createdAt;
}
