package woozlabs.echo.domain.sharedEmail.entity;

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
    private ShareStatus shareStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
