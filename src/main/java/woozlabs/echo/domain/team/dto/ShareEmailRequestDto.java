package woozlabs.echo.domain.team.dto;

import lombok.Getter;

@Getter
public class ShareEmailRequestDto {

    private String teamId;
    private String threadId;
    private String subject;
}
