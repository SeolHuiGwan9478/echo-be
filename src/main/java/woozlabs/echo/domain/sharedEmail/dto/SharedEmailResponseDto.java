package woozlabs.echo.domain.sharedEmail.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
