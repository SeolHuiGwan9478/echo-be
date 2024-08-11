package woozlabs.echo.domain.team.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import woozlabs.echo.domain.team.entity.SharedEmail;

public interface SharedEmailRepository extends MongoRepository<SharedEmail, String> {
}
