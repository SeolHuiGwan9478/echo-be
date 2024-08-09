package woozlabs.echo.domain.team.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import woozlabs.echo.domain.team.entity.TeamInvitation;

import java.util.Optional;

public interface TeamInvitationRepository extends JpaRepository<TeamInvitation, Long> {

    Optional<TeamInvitation> findByToken(String token);
}
