package woozlabs.echo.domain.team.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import woozlabs.echo.domain.team.entity.TeamSharedEmail;

public interface TeamSharedEmailRepository extends JpaRepository<TeamSharedEmail, Long> {
}
