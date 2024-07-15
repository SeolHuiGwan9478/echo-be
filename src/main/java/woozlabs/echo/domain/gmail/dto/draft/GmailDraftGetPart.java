package woozlabs.echo.domain.gmail.dto.draft;

import com.google.api.services.gmail.model.MessagePart;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class GmailDraftGetPart {
    private final String partId;
    private final String mimeType;
    private final String fileName;
    private final GmailDraftGetBody body;
    private final List<GmailDraftGetPart> parts;

    GmailDraftGetPart(MessagePart messagePart){
        this.partId = messagePart.getPartId();
        this.mimeType = messagePart.getMimeType();
        this.fileName = messagePart.getFilename();
        this.body = GmailDraftGetBody.toGmailDraftGetBody(messagePart.getBody());
        if(messagePart.getParts() != null) {
            this.parts = messagePart.getParts().stream().map(GmailDraftGetPart::new).toList();
        }else{
            this.parts = new ArrayList<>();
        }
    }
}
