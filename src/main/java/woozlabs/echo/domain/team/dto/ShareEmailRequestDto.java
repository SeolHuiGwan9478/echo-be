package woozlabs.echo.domain.team.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import woozlabs.echo.domain.team.dto.thread.ThreadGetResponse;

@Getter
@Setter
@NoArgsConstructor
public class ShareEmailRequestDto {

    private String teamId;
    private ThreadGetResponse gmailThread;
}
