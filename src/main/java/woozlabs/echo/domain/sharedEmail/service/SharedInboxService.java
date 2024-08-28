package woozlabs.echo.domain.sharedEmail.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import woozlabs.echo.domain.sharedEmail.dto.ShareEmailRequestDto;
import woozlabs.echo.domain.sharedEmail.entity.SharedEmail;
import woozlabs.echo.domain.sharedEmail.entity.Thread;
import woozlabs.echo.domain.sharedEmail.repository.SharedInboxRepository;
import woozlabs.echo.domain.sharedEmail.repository.ThreadRepository;
import woozlabs.echo.domain.team.entity.TeamAccount;
import woozlabs.echo.domain.team.service.TeamService;
import woozlabs.echo.domain.team.utils.AuthorizationUtil;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SharedInboxService {

    private final SharedInboxRepository sharedInboxRepository;
    private final ThreadRepository threadRepository;
    private final TeamService teamService;

    public List<SharedEmail> getSharedEmailsByTeam(String uid, String teamId) {
        teamService.getTeamMember(uid, Long.parseLong(teamId));
        return sharedInboxRepository.findByTeamId(teamId);
    }

    public Thread getSharedThread(String uid, String threadId) {
        SharedEmail sharedEmail = sharedInboxRepository.findByThreadId(threadId)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_SHARED_EMAIL));

        TeamAccount teamAccount =  teamService.getTeamMember(uid, Long.parseLong(sharedEmail.getTeamId()));
        if (!AuthorizationUtil.canViewSharedEmail(teamAccount, sharedEmail.getShareStatus())) {
            throw new CustomErrorException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        return threadRepository.findByThreadId(threadId);
    }

    @Transactional
    public void shareEmail(String sharedByUid, ShareEmailRequestDto shareEmailRequestDto) {
        TeamAccount teamAccount = teamService.getTeamMember(sharedByUid, Long.parseLong(shareEmailRequestDto.getTeamId()));
        if (!AuthorizationUtil.canSharedEmail(teamAccount)) {
            throw new CustomErrorException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        Thread thread = Thread.builder()
                .threadId(shareEmailRequestDto.getGmailThread().getId())
                .historyId(shareEmailRequestDto.getGmailThread().getHistoryId())
                .messages(shareEmailRequestDto.getGmailThread().getMessages())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Thread saveThread = threadRepository.save(thread);

        SharedEmail sharedEmail = SharedEmail.builder()
                .teamId(shareEmailRequestDto.getTeamId())
                .threadId(saveThread.getThreadId())
                .sharedById(sharedByUid)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        sharedInboxRepository.save(sharedEmail);
    }
}
