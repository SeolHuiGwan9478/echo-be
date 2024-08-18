package woozlabs.echo.domain.team.dto.thread;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ThreadGetBody {

    private String attachmentId;
    private int size;
    private String data;
}
