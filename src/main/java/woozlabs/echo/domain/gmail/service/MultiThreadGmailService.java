package woozlabs.echo.domain.gmail.service;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.*;
import com.google.api.services.gmail.model.Thread;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import woozlabs.echo.domain.gmail.dto.draft.GmailDraftListAttachments;
import woozlabs.echo.domain.gmail.dto.thread.GmailThreadGetMessagesBcc;
import woozlabs.echo.domain.gmail.dto.thread.GmailThreadGetMessagesCc;
import woozlabs.echo.domain.gmail.dto.thread.GmailThreadGetMessagesFrom;
import woozlabs.echo.domain.gmail.dto.thread.GmailThreadGetMessagesResponse;
import woozlabs.echo.domain.gmail.dto.thread.*;
import woozlabs.echo.domain.gmail.exception.GmailException;
import woozlabs.echo.domain.gmail.util.GmailUtility;
import woozlabs.echo.global.utils.GlobalUtility;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static woozlabs.echo.global.constant.GlobalConstant.*;

@Service
@RequiredArgsConstructor
public class MultiThreadGmailService {
    public GmailThreadListThreads multiThreadRequestGmailThreadGetForList(Thread thread, Gmail gmailService){
        try {
            // init
            String id = thread.getId();
            BigInteger historyId = thread.getHistoryId();
            GmailThreadListThreads gmailThreadListThreads = new GmailThreadListThreads();
            Thread detailedThread = gmailService.users().threads().get(USER_ID, id)
                    .setFormat(THREADS_GET_FULL_FORMAT)
                    .execute();
            List<Message> messages = detailedThread.getMessages();
            List<GmailThreadGetMessagesFrom> froms = new ArrayList<>();
            List<GmailThreadGetMessagesCc> ccs = new ArrayList<>();
            List<GmailThreadGetMessagesBcc> bccs = new ArrayList<>();
            Map<String, GmailThreadListAttachments> attachments = new HashMap<>();
            List<GmailThreadGetMessagesResponse> convertedMessages = new ArrayList<>();
            List<String> labelIds = new ArrayList<>();
            for(int idx = 0;idx < messages.size();idx++){
                int idxForLambda = idx;
                Message message = messages.get(idx);
                MessagePart payload = message.getPayload();
                convertedMessages.add(GmailThreadGetMessagesResponse.toGmailThreadGetMessages(message));
                List<MessagePartHeader> headers = payload.getHeaders(); // parsing header
                labelIds.addAll(message.getLabelIds());
                if(idxForLambda == messages.size()-1){
                    Long date = convertedMessages.get(convertedMessages.size()-1).getTimestamp();
                    gmailThreadListThreads.setSnippet(message.getSnippet());
                    gmailThreadListThreads.setTimestamp(date);
                }
                // get attachments
                getThreadsAttachments(payload, attachments);
                headers.forEach((header) -> {
                    String headerName = header.getName().toUpperCase();
                    // first message -> extraction subject
                    if (idxForLambda == 0 && headerName.equals(THREAD_PAYLOAD_HEADER_SUBJECT_KEY)) {
                        gmailThreadListThreads.setSubject(header.getValue());
                    }
                });
                GmailThreadGetMessagesResponse gmailThreadGetMessage = convertedMessages.get(convertedMessages.size()-1);
                froms.add(gmailThreadGetMessage.getFrom());
                ccs.addAll(gmailThreadGetMessage.getCc());
                bccs.addAll(gmailThreadGetMessage.getBcc());
            }
            gmailThreadListThreads.setLabelIds(labelIds.stream().distinct().collect(Collectors.toList()));
            gmailThreadListThreads.setId(id);
            gmailThreadListThreads.setHistoryId(historyId);
            gmailThreadListThreads.setFrom(froms.stream().distinct().toList());
            gmailThreadListThreads.setCc(ccs.stream().distinct().toList());
            gmailThreadListThreads.setBcc(bccs.stream().distinct().toList());
            gmailThreadListThreads.setThreadSize(messages.size());
            gmailThreadListThreads.setAttachments(attachments);
            gmailThreadListThreads.setAttachmentSize(attachments.size());
            gmailThreadListThreads.setMessages(convertedMessages);
            return gmailThreadListThreads;
        } catch (IOException e) {
            throw new GmailException(e.getMessage());
        }
    }

    private void getThreadsAttachments(MessagePart part, Map<String, GmailThreadListAttachments> attachments) throws IOException {
        if(part.getParts() == null){ // base condition
            if(part.getFilename() != null && !part.getFilename().isBlank() && !GlobalUtility.isInlineFile(part)){
                MessagePartBody body = part.getBody();
                List<MessagePartHeader> headers = part.getHeaders();
                GmailThreadListAttachments attachment = GmailThreadListAttachments.builder().build();
                String contentId = "";
                for(MessagePartHeader header : headers){
                    if(header.getName().toUpperCase().equals(THREAD_PAYLOAD_HEADER_CONTENT_ID_KEY)){
                        contentId = header.getValue();
                        contentId = contentId.replace("<", "").replace(">", "");
                    }
                }
                attachment.setMimeType(part.getMimeType());
                attachment.setAttachmentId(body.getAttachmentId());
                attachment.setSize(body.getSize());
                attachment.setFileName(part.getFilename());
                if(!attachments.containsKey(contentId)){
                    attachments.put(contentId, attachment);
                }
            }
        }else{ // recursion
            for(MessagePart subPart : part.getParts()){
                getThreadsAttachments(subPart, attachments);
            }
            if(part.getFilename() != null && !part.getFilename().isBlank() && !GlobalUtility.isInlineFile(part)){
                MessagePartBody body = part.getBody();
                List<MessagePartHeader> headers = part.getHeaders();
                GmailThreadListAttachments attachment = GmailThreadListAttachments.builder().build();
                String contentId = "";
                for(MessagePartHeader header : headers){
                    if(header.getName().toUpperCase().equals(THREAD_PAYLOAD_HEADER_CONTENT_ID_KEY)){
                        contentId = header.getValue();
                        contentId = contentId.replace("<", "").replace(">", "");
                    }
                }
                attachment.setMimeType(part.getMimeType());
                attachment.setAttachmentId(body.getAttachmentId());
                attachment.setSize(body.getSize());
                attachment.setFileName(part.getFilename());
                if(!attachments.containsKey(contentId)){
                    attachments.put(contentId, attachment);
                }
            }
        }
    }

    private void getDraftsAttachments(MessagePart part, List<GmailDraftListAttachments> attachments) throws IOException {
        if(part.getParts() == null){ // base condition
            if(part.getFilename() != null && !part.getFilename().isBlank() && !GlobalUtility.isInlineFile(part)){
                MessagePartBody body = part.getBody();
                List<MessagePartHeader> headers = part.getHeaders();
                GmailDraftListAttachments attachment = GmailDraftListAttachments.builder().build();
                for(MessagePartHeader header : headers){
                    if(header.getName().toUpperCase().equals(THREAD_PAYLOAD_HEADER_CONTENT_ID_KEY)){
                        String contentId = header.getValue();
                        contentId = contentId.replace("<", "").replace(">", "");
                        attachment.setContentId(contentId);
                    }
                }
                attachment.setMimeType(part.getMimeType());
                attachment.setAttachmentId(body.getAttachmentId());
                attachment.setSize(body.getSize());
                attachment.setFileName(part.getFilename());
                if(!attachments.contains(attachment)){
                    attachments.add(attachment);
                }
            }
        }else{ // recursion
            for(MessagePart subPart : part.getParts()){
                getDraftsAttachments(subPart, attachments);
            }
            if(part.getFilename() != null && !part.getFilename().isBlank() && !GlobalUtility.isInlineFile(part)){
                MessagePartBody body = part.getBody();
                List<MessagePartHeader> headers = part.getHeaders();
                GmailDraftListAttachments attachment = GmailDraftListAttachments.builder().build();
                for(MessagePartHeader header : headers){
                    if(header.getName().toUpperCase().equals(THREAD_PAYLOAD_HEADER_CONTENT_ID_KEY)){
                        String contentId = header.getValue();
                        contentId = contentId.replace("<", "").replace(">", "");
                        attachment.setContentId(contentId);
                    }
                }
                attachment.setMimeType(part.getMimeType());
                attachment.setAttachmentId(body.getAttachmentId());
                attachment.setSize(body.getSize());
                attachment.setFileName(part.getFilename());
                if(!attachments.contains(attachment)){
                    attachments.add(attachment);
                }
            }
        }
    }
}
