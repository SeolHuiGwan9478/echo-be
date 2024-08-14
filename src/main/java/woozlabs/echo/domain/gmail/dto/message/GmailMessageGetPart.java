package woozlabs.echo.domain.gmail.dto.message;

import com.google.api.services.gmail.model.MessagePart;
import lombok.Getter;
import woozlabs.echo.domain.gmail.dto.thread.GmailThreadGetBody;
import woozlabs.echo.domain.gmail.dto.thread.GmailThreadGetPart;

import java.util.ArrayList;
import java.util.List;

@Getter
public class GmailMessageGetPart {
    private final String partId;
    private final String mimeType;
    private final String fileName;
    private final GmailMessageGetBody body;
    private final List<GmailMessageGetPart> parts;

    GmailMessageGetPart(MessagePart messagePart){
        this.partId = messagePart.getPartId();
        this.mimeType = messagePart.getMimeType();
        this.fileName = messagePart.getFilename();
        this.body = GmailMessageGetBody.toGmailMessageGetBody(messagePart.getBody());
        if(messagePart.getParts() != null) {
            this.parts = messagePart.getParts().stream().map(GmailMessageGetPart::new).toList();
        }else{
            this.parts = new ArrayList<>();
        }
    }
}
