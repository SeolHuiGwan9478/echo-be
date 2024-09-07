package woozlabs.echo.domain.gmail.dto.thread;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

@Data
@Builder
@EqualsAndHashCode(of = "xAttachmentId")
public class GmailThreadListAttachments implements Comparable<GmailThreadListAttachments> {
    private String xAttachmentId;
    private String mimeType;
    private String fileName;
    private String attachmentId;
    private int size;

    @Override
    public int compareTo(@NotNull GmailThreadListAttachments o) {
        if(xAttachmentId.equals(o.attachmentId) &&
                mimeType.equals(o.mimeType) &&
                fileName.equals(o.fileName) &&
                attachmentId.equals(o.attachmentId) &&
                size == o.size) {
            return 0;
        }else if(xAttachmentId.length() < o.attachmentId.length()){
            return 1;
        }else{
            return -1;
        }
    }
}