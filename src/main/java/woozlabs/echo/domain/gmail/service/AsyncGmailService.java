package woozlabs.echo.domain.gmail.service;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.*;
import com.google.api.services.gmail.model.Thread;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import woozlabs.echo.domain.gmail.dto.GmailThreadListAttachments;
import woozlabs.echo.domain.gmail.dto.GmailThreadListThreads;
import woozlabs.echo.domain.gmail.exception.GmailException;

import java.io.IOException;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static woozlabs.echo.global.constant.GlobalConstant.*;
import static woozlabs.echo.global.utils.GlobalUtility.splitSenderData;

@Service
public class AsyncGmailService {
    @Async
    public CompletableFuture<GmailThreadListThreads> asyncRequestGmailThreadGetForList(Thread thread, Gmail gmailService){
        try {
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

            for(int idx = 0;idx < messages.size();idx++){
                int idxForLambda = idx;
                Message message = messages.get(idx);
                MessagePart payload = message.getPayload();
                List<MessagePartHeader> headers = payload.getHeaders(); // parsing header
                if(idxForLambda == 0){
                    gmailThreadListThreads.setLabelIds(message.getLabelIds());
                    gmailThreadListThreads.setMimeType(payload.getMimeType());
                }
                if(idxForLambda == messages.size()-1){
                    Long rawInternalDate = message.getInternalDate();
                    LocalDateTime internalDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(rawInternalDate), ZoneId.systemDefault());
                    gmailThreadListThreads.setSnippet(message.getSnippet());
                    gmailThreadListThreads.setInternalDate(internalDate);
                }
                // get attachments
                getAttachments(payload, attachments);
                headers.forEach((header) -> {
                    String headerName = header.getName();
                    // first message -> extraction subject
                    if (idxForLambda == 0 && headerName.equals(THREAD_PAYLOAD_HEADER_SUBJECT_KEY)){
                        gmailThreadListThreads.setSubject(header.getValue());
                    }
                    // all messages -> extraction emails & names
                    else if(headerName.equals(THREAD_PAYLOAD_HEADER_FROM_KEY)){
                            String sender = header.getValue();
                            List<String> splitSender = splitSenderData(sender);
                            if(!names.contains(splitSender.get(0))){
                                names.add(splitSender.get(0));
                                emails.add(splitSender.get(1));
                            }
                    }
                });
            }
            gmailThreadListThreads.setId(id);
            gmailThreadListThreads.setHistoryId(historyId);
            gmailThreadListThreads.setFromEmail(emails);
            gmailThreadListThreads.setFromName(names);
            gmailThreadListThreads.setThreadSize(messages.size());
            gmailThreadListThreads.setAttachments(attachments);
            gmailThreadListThreads.setAttachmentSize(attachments.size());
            return CompletableFuture.completedFuture(gmailThreadListThreads);
        } catch (IOException e) {
            throw new GmailException(e.getMessage());
        }
    }

    private void getAttachments(MessagePart part, List<GmailThreadListAttachments> attachments){
        if(part.getParts() == null){ // base condition
            if(part.getFilename() != null && !part.getFilename().isBlank()){
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
                getAttachments(subPart, attachments);
            }
            if(part.getFilename() != null && !part.getFilename().isBlank()){
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
}
