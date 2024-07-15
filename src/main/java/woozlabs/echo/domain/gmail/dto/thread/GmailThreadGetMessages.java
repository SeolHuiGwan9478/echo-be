package woozlabs.echo.domain.gmail.dto.thread;

import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
import lombok.Data;

import java.math.BigInteger;
import java.util.List;

import static woozlabs.echo.global.constant.GlobalConstant.*;
import static woozlabs.echo.global.utils.GlobalUtility.splitSenderData;

@Data
public class GmailThreadGetMessages {
    private String id; // message id
    private String date;
    private String fromName;
    private String fromEmail;
    private String threadId; // thread id
    private List<String> labelIds;
    private String snippet;
    private BigInteger historyId;
    private GmailThreadGetPayload payload;

    public static GmailThreadGetMessages toGmailThreadGetMessages(Message message){
        GmailThreadGetMessages gmailThreadGetMessages = new GmailThreadGetMessages();
        MessagePart payload = message.getPayload();
        GmailThreadGetPayload convertedPayload = new GmailThreadGetPayload(payload);
        List<MessagePartHeader> headers = payload.getHeaders(); // parsing header
        for(MessagePartHeader header: headers) {
            switch (header.getName()) {
                case THREAD_PAYLOAD_HEADER_FROM_KEY -> {
                    String sender = header.getValue();
                    List<String> splitSender = splitSenderData(sender);
                    if (splitSender.size() != 1){
                        gmailThreadGetMessages.setFromName(splitSender.get(0));
                        gmailThreadGetMessages.setFromEmail(splitSender.get(1));
                    }
                    else{
                        gmailThreadGetMessages.setFromEmail(splitSender.get(0));
                    }
                }
                case THREAD_PAYLOAD_HEADER_DATE_KEY -> gmailThreadGetMessages.setDate(header.getValue());
            }
        }
        gmailThreadGetMessages.setId(message.getId());
        gmailThreadGetMessages.setThreadId(message.getThreadId());
        gmailThreadGetMessages.setLabelIds(message.getLabelIds());
        gmailThreadGetMessages.setPayload(convertedPayload);
        return gmailThreadGetMessages;
    }
}