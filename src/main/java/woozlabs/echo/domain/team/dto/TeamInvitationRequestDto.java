package woozlabs.echo.domain.team.dto;

import lombok.Getter;
import lombok.Setter;
import woozlabs.echo.domain.team.entity.Role;

@Getter
@Setter
public class TeamInvitationRequestDto {

    private String inviteeEmail;
    private Role inviteeRole;
}
