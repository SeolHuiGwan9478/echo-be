package woozlabs.echo.domain.sharedEmail.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import woozlabs.echo.domain.sharedEmail.dto.thread.ThreadGetResponse;

@Getter
@Setter
@NoArgsConstructor
public class ShareEmailRequestDto {

    private String teamId;
    private ThreadGetResponse gmailThread;
}
