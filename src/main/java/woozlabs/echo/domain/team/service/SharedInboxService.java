package woozlabs.echo.domain.team.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import woozlabs.echo.domain.gmail.dto.thread.GmailThreadGetResponse;
import woozlabs.echo.domain.team.dto.ShareEmailRequestDto;
import woozlabs.echo.domain.team.entity.SharedEmail;
import woozlabs.echo.domain.team.entity.TeamMember;
import woozlabs.echo.domain.team.entity.Thread;
import woozlabs.echo.domain.team.repository.SharedInboxRepository;
import woozlabs.echo.domain.team.repository.ThreadRepository;
import woozlabs.echo.domain.team.utils.AuthorizationUtil;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

        TeamMember teamMember =  teamService.getTeamMember(uid, Long.parseLong(sharedEmail.getTeamId()));
        if (!AuthorizationUtil.canViewSharedEmail(teamMember, sharedEmail.getShareStatus())) {
            throw new CustomErrorException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        return threadRepository.findById(threadId).orElse(null);
    }

    @Transactional
    public void shareEmail(String sharedByUid, ShareEmailRequestDto shareEmailRequestDto) {
        TeamMember teamMember = teamService.getTeamMember(sharedByUid, Long.parseLong(shareEmailRequestDto.getTeamId()));
        if (!AuthorizationUtil.canEditSharedEmail(teamMember)) {
            throw new CustomErrorException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        Thread thread = Thread.builder()
                .id(shareEmailRequestDto.getGmailThread().getId())
                .historyId(shareEmailRequestDto.getGmailThread().getHistoryId())
                .messages(shareEmailRequestDto.getGmailThread().getMessages())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Thread saveThread = threadRepository.save(thread);

        SharedEmail sharedEmail = SharedEmail.builder()
                .teamId(shareEmailRequestDto.getTeamId())
                .threadId(saveThread.getId())
                .sharedById(sharedByUid)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        sharedInboxRepository.save(sharedEmail);
    }
}
