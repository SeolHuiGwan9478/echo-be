package woozlabs.echo.global.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import woozlabs.echo.domain.team.entity.TeamInvitation;
import woozlabs.echo.domain.team.repository.TeamInvitationRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JunkInvitationScheduler {

    private final TeamInvitationRepository teamInvitationRepository;

    @Scheduled(cron = "0 0 3 * * ?") // Run every day at 3 AM
    @Transactional
    public void removeExpiredInvitations() {
        LocalDateTime now = LocalDateTime.now();
        List<TeamInvitation> expiredInvitations = teamInvitationRepository.findExpiredInvitations(now);

        for (TeamInvitation invitation : expiredInvitations) {
            invitation.softDelete();
            invitation.setStatus(TeamInvitation.InvitationStatus.EXPIRED);
        }

        teamInvitationRepository.saveAll(expiredInvitations);

        int removedCount = expiredInvitations.size();
        log.info("Removed {} expired team invitations", removedCount);
    }
}
