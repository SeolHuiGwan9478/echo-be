package woozlabs.echo.domain.sharedEmail.dto.thread;

import lombok.Getter;

import java.util.List;

@Getter
public class ThreadGetPart {

    private String partId;
    private String mimeType;
    private String fileName;
    private ThreadGetBody body;
    private List<ThreadGetPart> parts;
}
