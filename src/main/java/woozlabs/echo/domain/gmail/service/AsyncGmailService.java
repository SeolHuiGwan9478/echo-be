package woozlabs.echo.domain.gmail.service;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.*;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import woozlabs.echo.domain.gmail.dto.draft.GmailDraftCommonRequest;
import woozlabs.echo.domain.gmail.dto.draft.GmailDraftCreateResponse;
import woozlabs.echo.domain.gmail.dto.message.GmailMessageGetResponse;
import woozlabs.echo.domain.gmail.util.GmailUtility;
import woozlabs.echo.domain.member.entity.Account;
import woozlabs.echo.domain.member.repository.AccountRepository;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static woozlabs.echo.global.constant.GlobalConstant.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class AsyncGmailService {
    private final String TEMP_FILE_PREFIX = "echo";
    private final AccountRepository accountRepository;
    private final GmailUtility gmailUtility;

    @Async
    public void createDraftForReplyTemplate(String uid, GmailDraftCommonRequest request, String threadId) throws Exception{
        Account account = accountRepository.findByUid(uid).orElseThrow(
                () -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCOUNT_ERROR_MESSAGE));
        String accessToken = account.getAccessToken();
        Gmail gmailService = gmailUtility.createGmailService(accessToken);
        Profile profile = gmailService.users().getProfile(USER_ID).execute();
        String fromEmailAddress = profile.getEmailAddress();
        request.setFromEmailAddress(fromEmailAddress);
        MimeMessage mimeMessage = createDraft(request);
        Message message = createMessage(mimeMessage, threadId);
        // create new draft
        Draft draft = new Draft().setMessage(message);
        Draft newDraft = gmailService.users().drafts().create(USER_ID, draft).execute();
        getOrCreateLabel(accessToken, newDraft.getMessage().getId());
    }

    private MimeMessage createDraft(GmailDraftCommonRequest request) throws MessagingException, IOException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        MimeMessage email = new MimeMessage(session);
        // setting base
        email.setFrom(new InternetAddress(request.getFromEmailAddress()));
        email.addRecipient(jakarta.mail.Message.RecipientType.TO,
                new InternetAddress(request.getToEmailAddress()));
        email.setSubject(request.getSubject());
        // setting body
        Multipart multipart = new MimeMultipart();
        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(request.getBodyText(), MULTI_PART_TEXT_PLAIN);
        multipart.addBodyPart(mimeBodyPart); // set bodyText
        List<MultipartFile> files = request.getFiles() != null ? request.getFiles() : new ArrayList<>();
        for(MultipartFile mimFile : files){
            MimeBodyPart fileMimeBodyPart = new MimeBodyPart();
            if(mimFile.getOriginalFilename() == null) throw new CustomErrorException(ErrorCode.REQUEST_GMAIL_USER_DRAFTS_SEND_API_ERROR_MESSAGE);
            File file = File.createTempFile(TEMP_FILE_PREFIX, mimFile.getOriginalFilename());
            mimFile.transferTo(file);
            DataSource source = new FileDataSource(file);
            fileMimeBodyPart.setFileName(mimFile.getOriginalFilename());
            fileMimeBodyPart.setDataHandler(new DataHandler(source));
            multipart.addBodyPart(fileMimeBodyPart);
            file.deleteOnExit();
        }
        email.setContent(multipart);
        return email;
    }


    private Message createMessage(MimeMessage emailContent, String threadId) throws MessagingException, IOException{
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        emailContent.writeTo(buffer);
        byte[] bytes = buffer.toByteArray();
        String encodedEmail = Base64.encodeBase64URLSafeString(bytes);
        Message message = new Message();
        message.setRaw(encodedEmail);
        message.setThreadId(threadId);
        return message;
    }

    private void getOrCreateLabel(String accessToken, String messageId) throws IOException {
        // find echo verification label
        Gmail gmailService = gmailUtility.createGmailService(accessToken);
        ListLabelsResponse listLabelsResponse = gmailService.users().labels().list(USER_ID).execute();
        for(Label label : listLabelsResponse.getLabels()){
            if(label.getName().equals(PARENT_VERIFICATION_LABEL + "/" + CHILD_AI_TEMPLATE_LABEL)){
                applyLabel(accessToken, messageId, label.getId());
                return;
            }
        }
        // create echo verification label
        try{
            Label parentLabel = new Label()
                    .setName(PARENT_VERIFICATION_LABEL)
                    .setLabelListVisibility("labelShow")
                    .setMessageListVisibility("show");
            gmailService.users().labels().create(USER_ID, parentLabel).execute();
        }catch (GoogleJsonResponseException e){
            log.info("Already exists Echo Label");
        }finally {
            Label childLabel = new Label()
                    .setName(PARENT_VERIFICATION_LABEL + "/" + CHILD_AI_TEMPLATE_LABEL)
                    .setLabelListVisibility("labelShow")
                    .setMessageListVisibility("show");
            childLabel = gmailService.users().labels().create(USER_ID, childLabel).execute();
            applyLabel(accessToken, messageId, childLabel.getId());
        }
    }

    private void applyLabel(String accessToken, String messageId, String labelId) throws IOException {
        Gmail gmailService = gmailUtility.createGmailService(accessToken);
        ModifyMessageRequest modifyMessageRequest = new ModifyMessageRequest().setAddLabelIds(Collections.singletonList(labelId));
        gmailService.users().messages().modify(USER_ID, messageId, modifyMessageRequest).execute();
    }
}
