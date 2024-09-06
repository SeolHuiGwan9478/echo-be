package woozlabs.echo.domain.team.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import woozlabs.echo.domain.team.entity.TeamSharedEmailPrivateComment;

import java.util.List;

public interface TeamSharedEmailPrivateCommentRepository extends MongoRepository<TeamSharedEmailPrivateComment, String> {

    List<TeamSharedEmailPrivateComment> findBySharedEmailId(String sharedEmailId);
}
