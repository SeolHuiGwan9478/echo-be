package woozlabs.echo.domain.gmail.dto.thread;

import com.google.api.services.gmail.model.MessagePart;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class GmailThreadGetPayload {
    private final String partId;
    private final String mimeType;
    private final String fileName;
    private final GmailThreadGetBody body;
    private final List<GmailThreadGetPart> parts;

    GmailThreadGetPayload(MessagePart messagePart){
        this.partId = messagePart.getPartId();
        this.mimeType = messagePart.getMimeType();
        this.fileName = messagePart.getFilename();
        this.body = GmailThreadGetBody.toGmailThreadGetBody(messagePart.getBody());
        if(messagePart.getParts() != null) {
            this.parts = messagePart.getParts().stream().map(GmailThreadGetPart::new).toList();
        }else{
            this.parts = new ArrayList<>();
        }
    }
}