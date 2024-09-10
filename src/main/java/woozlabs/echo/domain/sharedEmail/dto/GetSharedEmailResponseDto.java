package woozlabs.echo.domain.sharedEmail.dto;

import lombok.*;
import woozlabs.echo.domain.gmail.dto.thread.GmailThreadGetResponse;
import woozlabs.echo.domain.sharedEmail.entity.Permission;

import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetSharedEmailResponseDto {

    private GmailThreadGetResponse gmailThreadGetResponse;
    private Permission permissionLevel;
    private Boolean canEdit;
    private Boolean canViewToolMenu;
    private Map<String, Permission> inviteePermissions;
}
