package woozlabs.echo.domain.gmail.dto.draft;

import com.google.api.services.gmail.model.MessagePartBody;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GmailDraftGetBody {
    private String attachmentId;
    private int size;
    private String data;

    public static GmailDraftGetBody toGmailDraftGetBody(MessagePartBody body){
        return GmailDraftGetBody.builder()
                .attachmentId(body.getAttachmentId())
                .size(body.getSize())
                .data(body.getData())
                .build();
    }
}
