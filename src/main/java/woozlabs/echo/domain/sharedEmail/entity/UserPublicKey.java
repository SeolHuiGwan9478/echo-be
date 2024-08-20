package woozlabs.echo.domain.sharedEmail.entity;

import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_public_keys")
public class UserPublicKey {

    @Id
    private String uid;
    private String publicKey;
}
