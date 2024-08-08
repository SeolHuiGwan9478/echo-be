package woozlabs.echo.domain.team.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import woozlabs.echo.domain.team.entity.TeamInvitation;

public interface TeamInvitationRepository extends JpaRepository<TeamInvitation, Long> {
}
