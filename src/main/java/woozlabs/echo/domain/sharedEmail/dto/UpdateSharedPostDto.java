package woozlabs.echo.domain.sharedEmail.dto;

import lombok.*;
import woozlabs.echo.domain.sharedEmail.entity.Access;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSharedPostDto {

    private Access access;
    private Boolean canEditorEditPermission;
    private Boolean canViewerViewToolMenu;
}
