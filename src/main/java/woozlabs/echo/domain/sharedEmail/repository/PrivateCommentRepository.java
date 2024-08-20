package woozlabs.echo.domain.sharedEmail.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import woozlabs.echo.domain.sharedEmail.entity.PrivateComment;

import java.util.List;

public interface PrivateCommentRepository extends MongoRepository<PrivateComment, String> {

    List<PrivateComment> findBySharedEmailId(String sharedEmailId);
}
