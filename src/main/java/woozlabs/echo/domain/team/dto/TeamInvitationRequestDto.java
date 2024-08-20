package woozlabs.echo.domain.team.dto;

import lombok.Getter;
import woozlabs.echo.domain.team.entity.TeamMemberRole;

@Getter
public class TeamInvitationRequestDto {

    private String inviteeEmail;
    private TeamMemberRole inviteeTeamMemberRole;
}
