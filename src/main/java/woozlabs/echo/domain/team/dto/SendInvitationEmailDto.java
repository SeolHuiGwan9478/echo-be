package woozlabs.echo.domain.team.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SendInvitationEmailDto {

    private String to;
    private String inviterName;
    private String teamName;
    private String invitationLink;
}
