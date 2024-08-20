package woozlabs.echo.domain.sharedEmail.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import woozlabs.echo.domain.sharedEmail.entity.UserPublicKey;

public interface UserPublicKeyRepository extends MongoRepository<UserPublicKey, String> {

    UserPublicKey findByUid(String uid);
}
