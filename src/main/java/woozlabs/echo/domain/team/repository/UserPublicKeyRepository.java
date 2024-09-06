package woozlabs.echo.domain.team.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import woozlabs.echo.domain.team.entity.UserPublicKey;

public interface UserPublicKeyRepository extends MongoRepository<UserPublicKey, String> {

    UserPublicKey findByUid(String uid);
}
