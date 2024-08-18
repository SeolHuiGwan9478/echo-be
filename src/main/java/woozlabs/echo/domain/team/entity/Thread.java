package woozlabs.echo.domain.team.entity;

import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import woozlabs.echo.domain.team.dto.thread.ThreadGetMessages;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "threads")
public class Thread {
    @Id
    private String id;
    private BigInteger historyId;
    private List<ThreadGetMessages> messages;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
