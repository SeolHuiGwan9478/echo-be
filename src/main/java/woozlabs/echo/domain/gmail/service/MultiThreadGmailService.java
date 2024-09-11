package woozlabs.echo.domain.gmail.service;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.*;
import com.google.api.services.gmail.model.Thread;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import woozlabs.echo.domain.gmail.dto.draft.GmailDraftListAttachments;
import woozlabs.echo.domain.gmail.dto.draft.GmailDraftListDrafts;
import woozlabs.echo.domain.gmail.dto.message.GmailMessageInlineFileData;
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
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static woozlabs.echo.global.constant.GlobalConstant.*;
import static woozlabs.echo.global.utils.GlobalUtility.splitSenderData;

@Service
@RequiredArgsConstructor
public class MultiThreadGmailService {
    private final String VERIFICATION_EMAIL_LABEL = "VERIFICATION";
    private final GmailUtility gmailUtility;

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
            List<GmailThreadListAttachments> attachments = new ArrayList<>();
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
            //addVerificationLabel(convertedMessages, gmailThreadListThreads);
            return gmailThreadListThreads;
        } catch (IOException e) {
            throw new GmailException(e.getMessage());
        }
    }

    private void addVerificationLabel(List<GmailThreadGetMessagesResponse> convertedMessages, GmailThreadListThreads gmailThreadListThreads) {
        for(GmailThreadGetMessagesResponse convertedMessage : convertedMessages){
            if(convertedMessage.getVerification().getVerification()){
                gmailThreadListThreads.addLabel(VERIFICATION_EMAIL_LABEL);
                break;
            }
        }
    }

    @Async
    public CompletableFuture<GmailDraftListDrafts> asyncRequestGmailDraftGetForList(Draft draft, Gmail gmailService){
        try {
            String id = draft.getId();
            GmailDraftListDrafts gmailDraftListDrafts= new GmailDraftListDrafts();
            Draft detailedDraft = gmailService.users().drafts().get(USER_ID, id)
                    .setFormat(DRAFTS_GET_FULL_FORMAT)
                    .execute();
            Message message = detailedDraft.getMessage();;

            List<String> names = new ArrayList<>();
            List<String> emails = new ArrayList<>();
            List<GmailDraftListAttachments> attachments = new ArrayList<>();
            MessagePart payload = message.getPayload();
            List<MessagePartHeader> headers = payload.getHeaders(); // parsing header
            getDraftsAttachments(payload, attachments);

            headers.forEach((header) -> {
                String headerName = header.getName().toUpperCase();
                // first message -> extraction subject
                if (headerName.equals(DRAFT_PAYLOAD_HEADER_SUBJECT_KEY)){
                    gmailDraftListDrafts.setSubject(header.getValue());
                }
                // all messages -> extraction emails & names
                else if(headerName.equals(DRAFT_PAYLOAD_HEADER_FROM_KEY)){
                    String sender = header.getValue();
                    List<String> splitSender = splitSenderData(sender);
                    if(!names.contains(splitSender.get(0))){
                        names.add(splitSender.get(0));
                        emails.add(splitSender.get(1));
                    }
                }
            });

            gmailDraftListDrafts.setId(id);
            gmailDraftListDrafts.setFromEmail(emails);
            gmailDraftListDrafts.setFromName(names);
            gmailDraftListDrafts.setAttachments(attachments);
            gmailDraftListDrafts.setAttachmentSize(attachments.size());
            return CompletableFuture.completedFuture(gmailDraftListDrafts);
        } catch (IOException e) {
            throw new GmailException(e.getMessage());
        }
    }

    private void getThreadsAttachments(MessagePart part, List<GmailThreadListAttachments> attachments) throws IOException {
        if(part.getParts() == null){ // base condition
            if(part.getFilename() != null && !part.getFilename().isBlank() && !GlobalUtility.isInlineFile(part)){
                MessagePartBody body = part.getBody();
                List<MessagePartHeader> headers = part.getHeaders();
                GmailThreadListAttachments attachment = GmailThreadListAttachments.builder().build();
                for(MessagePartHeader header : headers){
                    if(header.getName().toUpperCase().equals(THREAD_PAYLOAD_HEADER_X_ATTACHMENT_ID_KEY)){
                        attachment.setXAttachmentId(header.getValue());
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
                getThreadsAttachments(subPart, attachments);
            }
            if(part.getFilename() != null && !part.getFilename().isBlank() && !GlobalUtility.isInlineFile(part)){
                MessagePartBody body = part.getBody();
                List<MessagePartHeader> headers = part.getHeaders();
                GmailThreadListAttachments attachment = GmailThreadListAttachments.builder().build();
                for(MessagePartHeader header : headers){
                    if(header.getName().toUpperCase().equals(THREAD_PAYLOAD_HEADER_X_ATTACHMENT_ID_KEY)){
                        attachment.setXAttachmentId(header.getValue());
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

    private void getDraftsAttachments(MessagePart part, List<GmailDraftListAttachments> attachments) throws IOException {
        if(part.getParts() == null){ // base condition
            if(part.getFilename() != null && !part.getFilename().isBlank() && !GlobalUtility.isInlineFile(part)){
                MessagePartBody body = part.getBody();
                attachments.add(GmailDraftListAttachments.builder()
                        .mimeType(part.getMimeType())
                        .fileName(part.getFilename())
                        .attachmentId(body.getAttachmentId())
                        .size(body.getSize()).build()
                );
            }
        }else{ // recursion
            for(MessagePart subPart : part.getParts()){
                getDraftsAttachments(subPart, attachments);
            }
            if(part.getFilename() != null && !part.getFilename().isBlank() && !GlobalUtility.isInlineFile(part)){
                MessagePartBody body = part.getBody();
                attachments.add(GmailDraftListAttachments.builder()
                        .mimeType(part.getMimeType())
                        .fileName(part.getFilename())
                        .attachmentId(body.getAttachmentId())
                        .size(body.getSize()).build()
                );
            }
        }
    }
}
