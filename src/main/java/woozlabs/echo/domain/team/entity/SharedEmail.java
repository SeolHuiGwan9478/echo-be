package woozlabs.echo.domain.team.entity;

import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
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
    private String teamId;  // MySQL의 Team 엔티티 ID 참조
    private String threadId;
    private String subject;
    private String sharedById;  // MySQL의 Member 엔티티 ID 참조
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
