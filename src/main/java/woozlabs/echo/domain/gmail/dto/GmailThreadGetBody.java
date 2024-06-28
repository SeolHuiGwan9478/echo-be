package woozlabs.echo.domain.gmail.dto;

import com.google.api.services.gmail.model.MessagePartBody;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GmailThreadGetBody {
    private String attachmentId;
    private int size;
    private String data;

    public static GmailThreadGetBody toGmailThreadGetBody(MessagePartBody body){
        return GmailThreadGetBody.builder()
                .attachmentId(body.getAttachmentId())
                .size(body.getSize())
                .data(body.getData())
                .build();
    }
}
