package woozlabs.echo.global.token.repository;

import org.springframework.data.repository.CrudRepository;
import woozlabs.echo.global.token.entity.RefreshToken;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
}
