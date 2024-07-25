package woozlabs.echo.domain.echo.dto.userSideBar;

import lombok.Data;

import java.util.List;

@Data
public class SidebarNavAccountDto {

    private String accountUid;
    private List<SpaceDto> spaces;
}
