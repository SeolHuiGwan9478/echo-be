package woozlabs.echo.domain.gmail.dto.message;

import com.google.api.services.gmail.model.MessagePartBody;
import lombok.Builder;
import lombok.Getter;
import woozlabs.echo.domain.gmail.dto.thread.GmailThreadGetBody;

@Getter
@Builder
public class GmailMessageGetBody {
    private String attachmentId;
    private int size;
    private String data;

    public static GmailMessageGetBody toGmailMessageGetBody(MessagePartBody body){
        return GmailMessageGetBody.builder()
                .attachmentId(body.getAttachmentId())
                .size(body.getSize())
                .data(body.getData())
                .build();
    }
}
