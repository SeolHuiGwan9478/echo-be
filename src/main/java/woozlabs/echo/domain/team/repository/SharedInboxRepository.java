package woozlabs.echo.domain.team.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import woozlabs.echo.domain.team.entity.SharedEmail;

import java.util.List;

public interface SharedInboxRepository extends MongoRepository<SharedEmail, String> {

    List<SharedEmail> findByTeamId(String teamId);
}
