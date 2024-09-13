package woozlabs.echo.domain.sharedEmail.dto;

import lombok.*;
import woozlabs.echo.domain.sharedEmail.entity.Permission;

import java.util.Map;

@Getter
@Setter
public class UpdateInviteePermissionsDto {
    private Map<String, Permission> inviteePermissions;
}
