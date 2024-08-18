package woozlabs.echo.domain.team.entity;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "user_public_keys")
public class UserPublicKey {

    @Id
    private String userId;
    private String publicKey;
}
