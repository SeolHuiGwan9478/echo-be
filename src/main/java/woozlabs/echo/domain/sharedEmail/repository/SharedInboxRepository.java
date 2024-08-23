package woozlabs.echo.domain.sharedEmail.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import woozlabs.echo.domain.sharedEmail.entity.SharedEmail;

import java.util.List;
import java.util.Optional;

public interface SharedInboxRepository extends MongoRepository<SharedEmail, String> {

    List<SharedEmail> findByTeamId(String teamId);

    Optional<SharedEmail> findByThreadId(String threadId);
}
