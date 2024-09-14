package woozlabs.echo.domain.sharedEmail.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import woozlabs.echo.domain.sharedEmail.entity.Access;
import woozlabs.echo.domain.sharedEmail.entity.Permission;
import woozlabs.echo.domain.sharedEmail.entity.SharedDataType;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharedEmailResponseDto {

    private UUID id;
    private Access access;
    private String dataId;
    private SharedDataType sharedDataType;
    private Boolean canEditorEditPermission;
    private Boolean canViewerViewToolMenu;
    private Map<String, Permission> inviteePermissions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
