package woozlabs.echo.domain.team.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import woozlabs.echo.domain.team.entity.Thread;

public interface ThreadRepository extends MongoRepository<Thread, String> {
}
