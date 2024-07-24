package woozlabs.echo.domain.echo.dto.userSideBar;

import lombok.Data;

@Data
public class ItemDto {

    private String type;
    private String id;
    private String title;
    private String icon;
    private String metadata;
    private String hotkey;
}
