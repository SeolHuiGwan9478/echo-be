package woozlabs.echo.domain.team.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import woozlabs.echo.domain.team.dto.TeamSharedEmailPrivateCommentCreateDto;
import woozlabs.echo.domain.team.dto.TeamSharedEmailPrivateCommentResponseDto;
import woozlabs.echo.domain.team.dto.UserPublicKeyDto;
import woozlabs.echo.domain.team.entity.TeamSharedEmail;
import woozlabs.echo.domain.team.entity.TeamSharedEmailPrivateComment;
import woozlabs.echo.domain.sharedEmail.entity.SharedEmail;
import woozlabs.echo.domain.team.entity.UserPublicKey;
import woozlabs.echo.domain.team.repository.TeamSharedEmailPrivateCommentRepository;
import woozlabs.echo.domain.sharedEmail.repository.SharedInboxRepository;
import woozlabs.echo.domain.team.repository.TeamSharedEmailRepository;
import woozlabs.echo.domain.team.repository.UserPublicKeyRepository;
import woozlabs.echo.domain.team.entity.TeamAccount;
import woozlabs.echo.domain.team.repository.TeamMemberRepository;
import woozlabs.echo.domain.team.utils.AuthorizationUtil;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamSharedEmailPrivateCommentService {

    private final TeamSharedEmailPrivateCommentRepository teamSharedEmailPrivateCommentRepository;
    private final UserPublicKeyRepository userPublicKeyRepository;
    private final TeamSharedEmailRepository teamSharedEmailRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamService teamService;

//    public List<TeamSharedEmailPrivateCommentResponseDto> getCommentsBySharedEmailId(String uid, String sharedEmailId) {
//        TeamSharedEmail teamsharedEmail = teamSharedEmailRepository.findById(sharedEmailId)
//                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_SHARED_EMAIL));
//
//        TeamAccount teamAccount = teamService.getTeamMember(uid, Long.parseLong(sharedEmail.getTeamId()));
//        if (!AuthorizationUtil.canViewSharedEmail(teamAccount, sharedEmail.getShareStatus())) {
//            throw new CustomErrorException(ErrorCode.UNAUTHORIZED_ACCESS);
//        }
//
//        List<TeamSharedEmailPrivateComment> comments = teamSharedEmailPrivateCommentRepository.findBySharedEmailId(sharedEmailId);
//
//        return comments.stream()
//                .map(TeamSharedEmailPrivateCommentResponseDto::new)
//                .collect(Collectors.toList());
//    }
//
//    @Transactional
//    public void createComment(String uid, TeamSharedEmailPrivateCommentCreateDto teamSharedEmailPrivateCommentCreateDto) {
//        SharedEmail sharedEmail = sharedInboxRepository.findById(teamSharedEmailPrivateCommentCreateDto.getSharedEmailId())
//                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_SHARED_EMAIL));
//
//        TeamAccount teamAccount = teamService.getTeamMember(uid, Long.parseLong(sharedEmail.getTeamId()));
//        if (!AuthorizationUtil.canViewSharedEmail(teamAccount, sharedEmail.getShareStatus())) {
//            throw new CustomErrorException(ErrorCode.UNAUTHORIZED_ACCESS);
//        }
//
//        TeamSharedEmailPrivateComment comment = TeamSharedEmailPrivateComment.builder()
//                .sharedEmailId(teamSharedEmailPrivateCommentCreateDto.getSharedEmailId())
//                .authorId(uid)
//                .encryptedContents(teamSharedEmailPrivateCommentCreateDto.getEncryptedContents())
//                .createdAt(LocalDateTime.now())
//                .updatedAt(null)
//                .build();
//
//        teamSharedEmailPrivateCommentRepository.save(comment);
//    }
//
//    @Transactional
//    public void saveUserPublicKey(String uid, String publicKey) {
//        UserPublicKey userPublicKey = UserPublicKey.builder()
//                .uid(uid)
//                .publicKey(publicKey)
//                .build();
//
//        userPublicKeyRepository.save(userPublicKey);
//    }
//
//    public List<UserPublicKeyDto> getPublicKeysForSharedEmail(String sharedEmailId) {
//        SharedEmail sharedEmail = sharedInboxRepository.findById(sharedEmailId)
//                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_SHARED_EMAIL));
//
//        List<TeamAccount> teamAccounts = teamMemberRepository.findByTeamId(Long.parseLong(sharedEmail.getTeamId()));
//
//        return teamAccounts.stream()
//                .map(member -> {
//                    UserPublicKey publicKey = userPublicKeyRepository.findByUid(member.getAccount().getUid());
//                    return new UserPublicKeyDto(publicKey);
//                })
//                .collect(Collectors.toList());
//    }
}
