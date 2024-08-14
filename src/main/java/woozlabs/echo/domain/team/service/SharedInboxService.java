package woozlabs.echo.domain.team.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import woozlabs.echo.domain.gmail.dto.thread.GmailThreadGetResponse;
import woozlabs.echo.domain.team.dto.ShareEmailRequestDto;
import woozlabs.echo.domain.team.entity.SharedEmail;
import woozlabs.echo.domain.team.entity.Thread;
import woozlabs.echo.domain.team.repository.SharedInboxRepository;
import woozlabs.echo.domain.team.repository.ThreadRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SharedInboxService {

    private final SharedInboxRepository sharedInboxRepository;
    private final ThreadRepository threadRepository;

    public List<SharedEmail> getSharedEmailsByTeam(String teamId) {
        return sharedInboxRepository.findByTeamId(teamId);
    }

    public Thread getSharedThread(String threadId) {
        return threadRepository.findById(threadId).orElse(null);
    }

    public void shareEmail(String sharedByUid, ShareEmailRequestDto shareEmailRequestDto) {

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
