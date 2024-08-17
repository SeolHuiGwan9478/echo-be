package woozlabs.echo.domain.team.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class PrivateCommentCreateDto {

    private String sharedEmailId;
    private String content;
    private Map<String, String> encryptedContents;
}
