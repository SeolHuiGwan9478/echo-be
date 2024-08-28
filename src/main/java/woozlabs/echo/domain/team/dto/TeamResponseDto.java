package woozlabs.echo.domain.team.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import woozlabs.echo.domain.team.entity.Team;
import woozlabs.echo.domain.team.entity.TeamAccount;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamResponseDto {

    private Long id;
    private String name;
    private String creatorUid;
    private List<TeamAccount> teamAccounts;

    public TeamResponseDto(Team team) {
        this.id = team.getId();
        this.name = team.getName();
        this.creatorUid = team.getCreator().getUid();
        this.teamAccounts = team.getTeamAccounts();
    }
}
