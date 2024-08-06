package woozlabs.echo.domain.team.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import woozlabs.echo.domain.team.entity.Team;

public interface TeamRepository extends JpaRepository<Team, Long> {
}
