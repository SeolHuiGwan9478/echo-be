package woozlabs.echo.global.token.repository;

import org.springframework.data.repository.CrudRepository;
import woozlabs.echo.global.token.entity.AccessToken;

public interface AccessTokenRepository extends CrudRepository<AccessToken, String> {
}
