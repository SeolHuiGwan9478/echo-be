package woozlabs.echo.domain.sharedEmail.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import woozlabs.echo.domain.sharedEmail.dto.PrivateCommentCreateDto;
import woozlabs.echo.domain.sharedEmail.dto.PrivateCommentResponseDto;
import woozlabs.echo.domain.sharedEmail.dto.UserPublicKeyDto;
import woozlabs.echo.domain.sharedEmail.entity.PrivateComment;
import woozlabs.echo.domain.sharedEmail.entity.SharedEmail;
import woozlabs.echo.domain.sharedEmail.entity.UserPublicKey;
import woozlabs.echo.domain.sharedEmail.repository.PrivateCommentRepository;
import woozlabs.echo.domain.sharedEmail.repository.SharedInboxRepository;
import woozlabs.echo.domain.sharedEmail.repository.UserPublicKeyRepository;
import woozlabs.echo.domain.team.entity.TeamAccount;
import woozlabs.echo.domain.team.repository.TeamMemberRepository;
import woozlabs.echo.domain.team.service.TeamService;
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
    private final UserPublicKeyRepository userPublicKeyRepository;
    private final SharedInboxRepository sharedInboxRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamService teamService;

    public List<PrivateCommentResponseDto> getCommentsBySharedEmailId(String uid, String sharedEmailId) {
        SharedEmail sharedEmail = sharedInboxRepository.findById(sharedEmailId)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_SHARED_EMAIL));

        TeamAccount teamAccount = teamService.getTeamMember(uid, Long.parseLong(sharedEmail.getTeamId()));
        if (!AuthorizationUtil.canViewSharedEmail(teamAccount, sharedEmail.getShareStatus())) {
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

        TeamAccount teamAccount = teamService.getTeamMember(uid, Long.parseLong(sharedEmail.getTeamId()));
        if (!AuthorizationUtil.canViewSharedEmail(teamAccount, sharedEmail.getShareStatus())) {
            throw new CustomErrorException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        PrivateComment comment = PrivateComment.builder()
                .sharedEmailId(privateCommentCreateDto.getSharedEmailId())
                .authorId(uid)
                .encryptedContents(privateCommentCreateDto.getEncryptedContents())
                .createdAt(LocalDateTime.now())
                .updatedAt(null)
                .build();

        privateCommentRepository.save(comment);
    }

    @Transactional
    public void saveUserPublicKey(String uid, String publicKey) {
        UserPublicKey userPublicKey = UserPublicKey.builder()
                .uid(uid)
                .publicKey(publicKey)
                .build();

        userPublicKeyRepository.save(userPublicKey);
    }

    public List<UserPublicKeyDto> getPublicKeysForSharedEmail(String sharedEmailId) {
        SharedEmail sharedEmail = sharedInboxRepository.findById(sharedEmailId)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_SHARED_EMAIL));

        List<TeamAccount> teamAccounts = teamMemberRepository.findByTeamId(Long.parseLong(sharedEmail.getTeamId()));

        return teamAccounts.stream()
                .map(member -> {
                    UserPublicKey publicKey = userPublicKeyRepository.findByUid(member.getAccount().getUid());
                    return new UserPublicKeyDto(publicKey);
                })
                .collect(Collectors.toList());
    }
}
