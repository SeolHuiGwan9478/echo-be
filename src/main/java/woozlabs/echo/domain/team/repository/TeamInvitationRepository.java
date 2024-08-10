package woozlabs.echo.domain.team.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import woozlabs.echo.domain.team.entity.TeamInvitation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TeamInvitationRepository extends JpaRepository<TeamInvitation, Long> {

    Optional<TeamInvitation> findByToken(String token);

    @Query("SELECT ti FROM TeamInvitation ti WHERE ti.expiresAt <= :now AND ti.status = 'PENDING' AND ti.deleted = false")
    List<TeamInvitation> findExpiredInvitations(@Param("now") LocalDateTime now);
}
