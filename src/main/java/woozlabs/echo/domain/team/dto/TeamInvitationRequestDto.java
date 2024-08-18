package woozlabs.echo.domain.team.dto;

import lombok.Getter;
import woozlabs.echo.domain.team.entity.Role;
import woozlabs.echo.domain.team.entity.TeamInvitation;

@Getter
public class TeamInvitationRequestDto {

    private String inviteeEmail;
    private Role inviteeRole;
}
