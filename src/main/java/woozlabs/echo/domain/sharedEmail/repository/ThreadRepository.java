package woozlabs.echo.domain.sharedEmail.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import woozlabs.echo.domain.sharedEmail.entity.Thread;

public interface ThreadRepository extends MongoRepository<Thread, String> {

    Thread findByThreadId(String threadId);
}
