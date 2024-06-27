package woozlabs.echo.global.auth.repository;

import org.springframework.data.repository.CrudRepository;
import woozlabs.echo.global.auth.token.RefreshToken;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
}
