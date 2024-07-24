package woozlabs.echo.domain.gmail.service;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.*;
import com.google.api.services.gmail.model.Thread;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import woozlabs.echo.domain.gmail.dto.draft.GmailDraftListAttachments;
import woozlabs.echo.domain.gmail.dto.draft.GmailDraftListDrafts;
import woozlabs.echo.domain.gmail.dto.thread.GmailThreadGetMessages;
import woozlabs.echo.domain.gmail.dto.thread.GmailThreadListAttachments;
import woozlabs.echo.domain.gmail.dto.thread.GmailThreadListThreads;
import woozlabs.echo.domain.gmail.exception.GmailException;

import java.io.IOException;
import java.math.BigInteger;
import java.time.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static woozlabs.echo.global.constant.GlobalConstant.*;
import static woozlabs.echo.global.utils.GlobalUtility.splitSenderData;

@Service
public class AsyncGmailService {
    private final String CONTENT_DISPOSITION_KEY = "Content-Disposition";
    private final String CONTENT_DISPOSITION_INLINE_VALUE = "inline";

    @Async
    public CompletableFuture<GmailThreadListThreads> asyncRequestGmailThreadGetForList(Thread thread, Gmail gmailService){
        try {
            // init
            String id = thread.getId();
            BigInteger historyId = thread.getHistoryId();
            GmailThreadListThreads gmailThreadListThreads= new GmailThreadListThreads();
            Thread detailedThread = gmailService.users().threads().get(USER_ID, id)
                    .setFormat(THREADS_GET_FULL_FORMAT)
                    .execute();
            List<Message> messages = detailedThread.getMessages();
            List<String> names = new ArrayList<>();
            List<String> emails = new ArrayList<>();
            List<GmailThreadListAttachments> attachments = new ArrayList<>();
            List<GmailThreadGetMessages> convertedMessages = new ArrayList<>();
            List<String> labelIds = new ArrayList<>();
            for(int idx = 0;idx < messages.size();idx++){
                int idxForLambda = idx;
                Message message = messages.get(idx);
                MessagePart payload = message.getPayload();
                convertedMessages.add(GmailThreadGetMessages.toGmailThreadGetMessages(message));
                List<MessagePartHeader> headers = payload.getHeaders(); // parsing header
                labelIds.addAll(message.getLabelIds());
                if(idxForLambda == messages.size()-1){
                    Long rawInternalDate = message.getInternalDate();
                    LocalDateTime internalDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(rawInternalDate), ZoneId.systemDefault());
                    ZonedDateTime zonedDateTime = internalDate.atZone(ZoneId.systemDefault());
                    // Convert the ZonedDateTime to UTC
                    ZonedDateTime utcDateTime = zonedDateTime.withZoneSameInstant(ZoneOffset.UTC);
                    // Convert back to LocalDateTime if necessary
                    LocalDateTime utcLocalDateTime = utcDateTime.toLocalDateTime();
                    gmailThreadListThreads.setSnippet(message.getSnippet());
                    gmailThreadListThreads.setInternalDate(utcLocalDateTime);
                }
                // get attachments
                getThreadsAttachments(payload, attachments);
                headers.forEach((header) -> {
                    String headerName = header.getName();
                    // first message -> extraction subject
                    if (idxForLambda == 0 && headerName.equals(THREAD_PAYLOAD_HEADER_SUBJECT_KEY)){
                        gmailThreadListThreads.setSubject(header.getValue());
                    } else if(headerName.equals(THREAD_PAYLOAD_HEADER_FROM_KEY)){ // all messages -> extraction emails & names
                        String sender = header.getValue();
                        List<String> splitSender = splitSenderData(sender);
                        if(!names.contains(splitSender.get(0))){
                            names.add(splitSender.get(0));
                            emails.add(splitSender.get(1));
                        }
                    }
                });
            }
            gmailThreadListThreads.setLabelIds(labelIds.stream().distinct().collect(Collectors.toList()));
            gmailThreadListThreads.setId(id);
            gmailThreadListThreads.setHistoryId(historyId);
            gmailThreadListThreads.setFromEmail(emails);
            gmailThreadListThreads.setFromName(names);
            gmailThreadListThreads.setThreadSize(messages.size());
            gmailThreadListThreads.setAttachments(attachments);
            gmailThreadListThreads.setAttachmentSize(attachments.size());
            gmailThreadListThreads.setMessages(convertedMessages);
            return CompletableFuture.completedFuture(gmailThreadListThreads);
        } catch (IOException e) {
            throw new GmailException(e.getMessage());
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
                String headerName = header.getName();
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

    private void getThreadsAttachments(MessagePart part, List<GmailThreadListAttachments> attachments){
        if(part.getParts() == null){ // base condition
            if(part.getFilename() != null && !part.getFilename().isBlank() && !isInlineFile(part)){
                MessagePartBody body = part.getBody();
                attachments.add(GmailThreadListAttachments.builder()
                        .mimeType(part.getMimeType())
                        .fileName(part.getFilename())
                        .attachmentId(body.getAttachmentId())
                        .size(body.getSize()).build()
                );
            }
        }else{ // recursion
            for(MessagePart subPart : part.getParts()){
                getThreadsAttachments(subPart, attachments);
            }
            if(part.getFilename() != null && !part.getFilename().isBlank() && !isInlineFile(part)){
                MessagePartBody body = part.getBody();
                attachments.add(GmailThreadListAttachments.builder()
                        .mimeType(part.getMimeType())
                        .fileName(part.getFilename())
                        .attachmentId(body.getAttachmentId())
                        .size(body.getSize()).build()
                );
            }
        }
    }

    private Boolean isInlineFile(MessagePart part){
        List<MessagePartHeader> headers = part.getHeaders();
        for(MessagePartHeader header : headers){
            if(header.getName().equals(CONTENT_DISPOSITION_KEY)){
                String[] parts = header.getValue().split(";");
                String inlinePart = parts[0].trim();
                if(inlinePart.equals(CONTENT_DISPOSITION_INLINE_VALUE)) return Boolean.TRUE;
                return Boolean.FALSE;
            }
        }
        return Boolean.FALSE;
    }

    private void getDraftsAttachments(MessagePart part, List<GmailDraftListAttachments> attachments){
        if(part.getParts() == null){ // base condition
            if(part.getFilename() != null && !part.getFilename().isBlank() && !isInlineFile(part)){
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
            if(part.getFilename() != null && !part.getFilename().isBlank() && !isInlineFile(part)){
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
