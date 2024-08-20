package woozlabs.echo.domain.sharedEmail.dto.thread;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ThreadGetPayload {

    private String partId;
    private String mimeType;
    private String fileName;
    private ThreadGetBody body;
    private List<ThreadGetPart> parts;
}
