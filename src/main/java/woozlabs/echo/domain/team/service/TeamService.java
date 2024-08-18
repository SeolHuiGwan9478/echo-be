package woozlabs.echo.domain.team.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import woozlabs.echo.domain.member.entity.Member;
import woozlabs.echo.domain.member.repository.MemberRepository;
import woozlabs.echo.domain.team.dto.CreateTeamRequestDto;
import woozlabs.echo.domain.team.dto.SendInvitationEmailDto;
import woozlabs.echo.domain.team.dto.TeamInvitationRequestDto;
import woozlabs.echo.domain.team.dto.TeamResponseDto;
import woozlabs.echo.domain.team.entity.Role;
import woozlabs.echo.domain.team.entity.Team;
import woozlabs.echo.domain.team.entity.TeamInvitation;
import woozlabs.echo.domain.team.entity.TeamMember;
import woozlabs.echo.domain.team.repository.TeamInvitationRepository;
import woozlabs.echo.domain.team.repository.TeamMemberRepository;
import woozlabs.echo.domain.team.repository.TeamRepository;
import woozlabs.echo.global.constant.GlobalConstant;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamService {

    private final TeamRepository teamRepository;
    private final MemberRepository memberRepository;
    private final TeamInvitationRepository teamInvitationRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final EmailService emailService;

    public List<TeamResponseDto> getTeams(String uid) {
        List<Team> teams = teamRepository.findAllTeamsByMemberUid(uid);

        return teams.stream()
                .map(TeamResponseDto::new)
                .collect(Collectors.toList());
    }

    public TeamMember getTeamMember(String uid, Long teamId) {
        Member member = memberRepository.findByUid(uid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER_ERROR_MESSAGE));

        return teamMemberRepository.findByMemberAndTeamId(member, teamId)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_TEAM_MEMBER));
    }

    @Transactional
    public void createTeam(String uid, CreateTeamRequestDto createTeamRequestDto) {
        Member creator = memberRepository.findByUid(uid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER_ERROR_MESSAGE));

        Team team = Team.builder()
                .name(createTeamRequestDto.getName())
                .creator(creator)
                .build();

        TeamMember creatorMember = TeamMember.builder()
                .team(team)
                .member(creator)
                .role(Role.ADMIN)
                .build();

        team.addTeamMember(creatorMember);
        teamRepository.save(team);
    }

    @Transactional
    public void inviteToTeam(String inviterUid, Long teamId, TeamInvitationRequestDto requestDto) {
        Member inviter = memberRepository.findByUid(inviterUid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER_ERROR_MESSAGE));

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_TEAM));

        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);  // 7일 후 만료

        TeamInvitation invitation = TeamInvitation.builder()
                .team(team)
                .inviter(inviter)
                .inviteeEmail(requestDto.getInviteeEmail())
                .token(token)
                .expiresAt(expiresAt)
                .sentAt(LocalDateTime.now())
                .inviteeRole(requestDto.getInviteeRole())
                .build();

        teamInvitationRepository.save(invitation);

        String invitationLink = GlobalConstant.ECHO_NEXT_APP_DOMAIN + "/invite?token=" + token;

        SendInvitationEmailDto sendInvitationEmailDto = SendInvitationEmailDto.builder()
                .to(requestDto.getInviteeEmail())
                .username(requestDto.getInviteeEmail())
                .userImage("https://vercel.com/static/vercel-user.png") // mock
                .invitedByUsername(inviter.getDisplayName())
                .invitedByEmail(inviter.getEmail())
                .teamName(team.getName())
                .teamRole(requestDto.getInviteeRole())
                .teamImage("https://vercel.com/static/vercel-team.png") // mock
                .inviteLink(invitationLink)
                .build();

        emailService.sendEmailViaSES(sendInvitationEmailDto);
    }

    @Transactional
    public void acceptInvitation(String inviteeUid, String token) {
        TeamInvitation teamInvitation = teamInvitationRepository.findByToken(token)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_INVITATION_TOKEN));

        if (teamInvitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new CustomErrorException(ErrorCode.INVITATION_EXPIRED);
        }

        Member invitee = memberRepository.findByUid(inviteeUid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER_ERROR_MESSAGE));

        TeamMember newTeamMember = TeamMember.builder()
                .team(teamInvitation.getTeam())
                .member(invitee)
                .role(teamInvitation.getRole())
                .build();

        teamInvitation.getTeam().addTeamMember(newTeamMember);
        teamInvitation.accept();

        teamInvitation.softDelete();
        teamInvitationRepository.save(teamInvitation);
    }
}
