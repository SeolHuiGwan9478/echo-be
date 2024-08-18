package woozlabs.echo.domain.team.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import woozlabs.echo.domain.team.entity.Role;

@Getter
@Setter
@Builder
public class SendInvitationEmailDto {

    private String to;
    private String username;
    private String userImage;
    private String invitedByUsername;
    private String invitedByEmail;
    private String teamName;
    private Role teamRole;
    private String teamImage;
    private String inviteLink;
}
