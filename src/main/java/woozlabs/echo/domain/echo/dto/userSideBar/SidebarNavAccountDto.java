package woozlabs.echo.domain.echo.dto.userSideBar;

import lombok.Data;

import java.util.List;

@Data
public class SidebarNavAccountDto {

    private Long id;
    private String accountName;
    private List<SpaceDto> spaces;
}
