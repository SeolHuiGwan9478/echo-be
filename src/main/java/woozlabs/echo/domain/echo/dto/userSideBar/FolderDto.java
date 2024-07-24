package woozlabs.echo.domain.echo.dto.userSideBar;

import lombok.Data;

import java.util.List;

@Data
public class FolderDto {

    private String type;
    private String id;
    private String title;
    private List<ItemDto> items;
}
