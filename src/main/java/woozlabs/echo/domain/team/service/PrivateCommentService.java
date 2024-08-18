package woozlabs.echo.domain.team.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import woozlabs.echo.domain.team.dto.PrivateCommentCreateDto;
import woozlabs.echo.domain.team.dto.PrivateCommentResponseDto;
import woozlabs.echo.domain.team.entity.PrivateComment;
import woozlabs.echo.domain.team.entity.SharedEmail;
import woozlabs.echo.domain.team.entity.TeamMember;
import woozlabs.echo.domain.team.repository.PrivateCommentRepository;
import woozlabs.echo.domain.team.repository.SharedInboxRepository;
import woozlabs.echo.domain.team.utils.AuthorizationUtil;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PrivateCommentService {

    private final PrivateCommentRepository privateCommentRepository;
    private final SharedInboxRepository sharedInboxRepository;
    private final TeamService teamService;

    public List<PrivateCommentResponseDto> getCommentsBySharedEmailId(String uid, String sharedEmailId) {
        SharedEmail sharedEmail = sharedInboxRepository.findById(sharedEmailId)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_SHARED_EMAIL));

        TeamMember teamMember = teamService.getTeamMember(uid, Long.parseLong(sharedEmail.getTeamId()));
        if (!AuthorizationUtil.canViewSharedEmail(teamMember, sharedEmail.getShareStatus())) {
            throw new CustomErrorException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        List<PrivateComment> comments = privateCommentRepository.findBySharedEmailId(sharedEmailId);

        return comments.stream()
                .map(PrivateCommentResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void createComment(String uid, PrivateCommentCreateDto privateCommentCreateDto) {
        SharedEmail sharedEmail = sharedInboxRepository.findById(privateCommentCreateDto.getSharedEmailId())
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_SHARED_EMAIL));

        TeamMember teamMember = teamService.getTeamMember(uid, Long.parseLong(sharedEmail.getTeamId()));
        if (!AuthorizationUtil.canViewSharedEmail(teamMember, sharedEmail.getShareStatus())) {
            throw new CustomErrorException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        PrivateComment comment = PrivateComment.builder()
                .sharedEmailId(privateCommentCreateDto.getSharedEmailId())
                .authorId(uid)
                .encryptedContents(privateCommentCreateDto.getEncryptedContents())
                .createdAt(LocalDateTime.now())
                .build();

        privateCommentRepository.save(comment);
    }
}
