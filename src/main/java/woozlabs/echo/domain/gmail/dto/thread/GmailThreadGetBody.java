package woozlabs.echo.domain.gmail.dto.thread;

import com.google.api.services.gmail.model.MessagePartBody;
import com.google.api.services.gmail.model.MessagePartHeader;
import lombok.Builder;
import lombok.Getter;
import woozlabs.echo.domain.gmail.util.GmailUtility;
import woozlabs.echo.global.utils.GlobalUtility;

import java.util.List;

@Getter
@Builder
public class GmailThreadGetBody {
    private String attachmentId;
    private int size;
    private String data;

    public static GmailThreadGetBody toGmailThreadGetBody(MessagePartBody body){
        String data = body.getData();
        if(data != null) data = GlobalUtility.decodeAndReEncodeEmail(data);
        return GmailThreadGetBody.builder()
                .attachmentId(body.getAttachmentId())
                .size(body.getSize())
                .data(data)
                .build();
    }
}
