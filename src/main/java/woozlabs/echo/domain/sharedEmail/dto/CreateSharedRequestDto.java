package woozlabs.echo.domain.sharedEmail.dto;

import lombok.*;
import woozlabs.echo.domain.sharedEmail.entity.Access;
import woozlabs.echo.domain.sharedEmail.entity.SharedDataType;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSharedRequestDto {

    private String dataId;
    private SharedDataType sharedDataType;
    private boolean canEditorEditPermission;
    private boolean canViewerViewToolMenu;
    private Access access;
}