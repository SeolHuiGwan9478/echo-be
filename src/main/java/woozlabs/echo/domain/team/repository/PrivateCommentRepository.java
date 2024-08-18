package woozlabs.echo.domain.team.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import woozlabs.echo.domain.team.entity.PrivateComment;

import java.util.List;

public interface PrivateCommentRepository extends MongoRepository<PrivateComment, String> {

    List<PrivateComment> findBySharedEmailId(String sharedEmailId);
}
