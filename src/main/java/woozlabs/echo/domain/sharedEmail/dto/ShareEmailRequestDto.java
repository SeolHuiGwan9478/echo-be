package woozlabs.echo.domain.sharedEmail.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import woozlabs.echo.domain.sharedEmail.entity.Access;
import woozlabs.echo.domain.sharedEmail.entity.Permission;
import woozlabs.echo.domain.sharedEmail.entity.SharedDataType;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ShareEmailRequestDto {

    private Permission permission;
    private Access access;
    private String dataId;
    private SharedDataType sharedDataType;
    private boolean notifyInvitation;
    private boolean canEditorEditPermission;
    private boolean canViewerViewToolMenu;
    private List<String> invitees;
    private String invitationMemo;
}
