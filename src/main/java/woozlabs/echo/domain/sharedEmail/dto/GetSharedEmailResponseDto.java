package woozlabs.echo.domain.sharedEmail.dto;

import lombok.*;
import woozlabs.echo.domain.sharedEmail.entity.Permission;
import woozlabs.echo.domain.sharedEmail.entity.SharedDataType;

import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetSharedEmailResponseDto {

    private Object sharedEmailData;
    private String dataId;
    private Permission permissionLevel;
    private Boolean canEdit;
    private Boolean canViewToolMenu;
    private SharedDataType sharedDataType;
    private Map<String, Permission> inviteePermissions;
}
