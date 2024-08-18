package woozlabs.echo.domain.team.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import woozlabs.echo.domain.team.entity.SharedEmail;

import java.util.List;
import java.util.Optional;

public interface SharedInboxRepository extends MongoRepository<SharedEmail, String> {

    List<SharedEmail> findByTeamId(String teamId);

    Optional<SharedEmail> findByThreadId(String threadId);
}
