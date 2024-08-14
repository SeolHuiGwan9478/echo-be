package woozlabs.echo.domain.team.entity;

import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "shared_emails")
public class SharedEmail {

    @Id
    private String id;
    private String teamId;
    private String threadId;
    private String sharedById;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
