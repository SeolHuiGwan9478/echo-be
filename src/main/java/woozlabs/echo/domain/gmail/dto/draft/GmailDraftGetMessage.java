package woozlabs.echo.domain.gmail.dto.draft;

import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
import lombok.Data;

import java.math.BigInteger;
import java.util.List;

import static woozlabs.echo.global.constant.GlobalConstant.*;
import static woozlabs.echo.global.utils.GlobalUtility.splitSenderData;

@Data
public class GmailDraftGetMessage {
    private String id; // message id
    private String timestamp;
    private String fromName;
    private String fromEmail;
    private String threadId; // thread id
    private List<String> labelIds;
    private String snippet;
    private BigInteger historyId;
    private GmailDraftGetPayload payload;

    public static GmailDraftGetMessage toGmailDraftGetMessages(Message message){
        GmailDraftGetMessage gmailDraftGetMessages = new GmailDraftGetMessage();
        MessagePart payload = message.getPayload();
        GmailDraftGetPayload convertedPayload = new GmailDraftGetPayload(payload);
        List<MessagePartHeader> headers = payload.getHeaders(); // parsing header
        for(MessagePartHeader header: headers) {
            switch (header.getName()) {
                case DRAFT_PAYLOAD_HEADER_FROM_KEY -> {
                    String sender = header.getValue();
                    List<String> splitSender = splitSenderData(sender);
                    if (splitSender.size() != 1){
                        gmailDraftGetMessages.setFromName(splitSender.get(0));
                        gmailDraftGetMessages.setFromEmail(splitSender.get(1));
                    }
                    else{
                        gmailDraftGetMessages.setFromEmail(splitSender.get(0));
                    }
                }
                case DRAFT_PAYLOAD_HEADER_DATE_KEY -> gmailDraftGetMessages.setTimestamp(header.getValue());
            }
        }
        gmailDraftGetMessages.setId(message.getId());
        gmailDraftGetMessages.setThreadId(message.getThreadId());
        gmailDraftGetMessages.setLabelIds(message.getLabelIds());
        gmailDraftGetMessages.setPayload(convertedPayload);
        return gmailDraftGetMessages;
    }
}
