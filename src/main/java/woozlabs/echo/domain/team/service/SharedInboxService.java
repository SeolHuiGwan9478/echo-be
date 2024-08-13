package woozlabs.echo.domain.team.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import woozlabs.echo.domain.team.dto.ShareEmailRequestDto;
import woozlabs.echo.domain.team.entity.SharedEmail;
import woozlabs.echo.domain.team.repository.SharedInboxRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SharedInboxService {

    private final SharedInboxRepository sharedInboxRepository;

    public List<SharedEmail> getSharedEmailsByTeam(String teamId) {
        return sharedInboxRepository.findByTeamId(teamId);
    }

    public void shareEmail(String sharedByUid, ShareEmailRequestDto shareEmailRequestDto) {
        SharedEmail sharedEmail = SharedEmail.builder()
                .teamId(shareEmailRequestDto.getTeamId())
                .threadId(shareEmailRequestDto.getThreadId())
                .subject(shareEmailRequestDto.getSubject())
                .sharedById(sharedByUid)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        sharedInboxRepository.save(sharedEmail);
    }
}
