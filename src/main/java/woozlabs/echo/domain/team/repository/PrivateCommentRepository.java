package woozlabs.echo.domain.team.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import woozlabs.echo.domain.team.entity.PrivateComment;

public interface PrivateCommentRepository extends MongoRepository<PrivateComment, String> {
}
