package woozlabs.echo.domain.team.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import woozlabs.echo.domain.member.entity.Member;
import woozlabs.echo.domain.member.repository.MemberRepository;
import woozlabs.echo.domain.team.dto.CreateTeamRequestDto;
import woozlabs.echo.domain.team.dto.TeamResponseDto;
import woozlabs.echo.domain.team.entity.Team;
import woozlabs.echo.domain.team.entity.TeamMember;
import woozlabs.echo.domain.team.repository.TeamRepository;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamService {

    private final TeamRepository teamRepository;
    private final MemberRepository memberRepository;

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
                .role(TeamMember.TeamMemberRole.ADMIN)
                .build();

        team.addTeamMember(creatorMember);
        teamRepository.save(team);
    }

    public List<TeamResponseDto> getTeams(String uid) {
        List<Team> teams = teamRepository.findAllTeamsByMemberUid(uid);

        return teams.stream()
                .map(TeamResponseDto::new)
                .collect(Collectors.toList());
    }
}
