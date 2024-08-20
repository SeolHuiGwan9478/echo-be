package woozlabs.echo.domain.sharedEmail.dto.thread;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ThreadGetBody {

    private String attachmentId;
    private int size;
    private String data;
}
