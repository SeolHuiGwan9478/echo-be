package woozlabs.echo.domain.sharedEmail.entity;

import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import woozlabs.echo.domain.sharedEmail.dto.thread.ThreadGetMessages;

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
    private String threadId;
    private BigInteger historyId;
    private List<ThreadGetMessages> messages;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
