package woozlabs.echo.domain.sharedEmail.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ExcludeInviteesRequestDto {

    private List<String> inviteeEmails;
}
