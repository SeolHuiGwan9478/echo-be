package woozlabs.echo.domain.team.dto;

import lombok.Builder;
import lombok.Getter;
import woozlabs.echo.domain.team.entity.Team;
import woozlabs.echo.domain.team.entity.TeamMember;

import java.util.List;

@Getter
@Builder
public class TeamResponseDto {

    private Long id;
    private String name;
    private String creatorUid;
    private List<TeamMember> teamMembers;

    public TeamResponseDto(Team team) {
        this.id = team.getId();
        this.name = team.getName();
        this.creatorUid = team.getCreator().getUid();
        this.teamMembers = team.getTeamMembers();
    }
}
