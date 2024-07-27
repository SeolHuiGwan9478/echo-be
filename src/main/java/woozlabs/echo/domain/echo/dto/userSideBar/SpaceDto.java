package woozlabs.echo.domain.echo.dto.userSideBar;

import lombok.Data;

import java.util.List;

@Data
public class SpaceDto {

    private String id;
    private String title;
    private List<FolderDto> folders;
}
