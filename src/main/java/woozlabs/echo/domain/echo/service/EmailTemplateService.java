package woozlabs.echo.domain.echo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import woozlabs.echo.domain.echo.dto.emailTemplate.CreateEmailTemplateRequest;
import woozlabs.echo.domain.echo.dto.emailTemplate.EmailTemplateResponse;
import woozlabs.echo.domain.echo.dto.emailTemplate.UpdateEmailTemplateRequest;
import woozlabs.echo.domain.echo.entity.EmailRecipient;
import woozlabs.echo.domain.echo.entity.EmailTemplate;
import woozlabs.echo.domain.echo.repository.EmailTemplateRepository;
import woozlabs.echo.domain.member.entity.Account;
import woozlabs.echo.domain.member.repository.AccountRepository;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EmailTemplateService {

    private final EmailTemplateRepository emailTemplateRepository;
    private final AccountRepository accountRepository;
    public List<EmailTemplateResponse> getAllTemplates(String uid) {
        Account account = accountRepository.findByUid(uid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCOUNT_ERROR_MESSAGE));

        List<EmailTemplate> emailTemplates = emailTemplateRepository.findByAccount(account);
        return emailTemplates.stream()
                .map(EmailTemplateResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void createTemplate(String uid, CreateEmailTemplateRequest createEmailTemplateRequest) {
        Account account = accountRepository.findByUid(uid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCOUNT_ERROR_MESSAGE));

        EmailTemplate emailTemplate = new EmailTemplate();
        emailTemplate.setTemplateName(createEmailTemplateRequest.getTemplateName());
        emailTemplate.setSubject(createEmailTemplateRequest.getSubject());
        emailTemplate.setBody(createEmailTemplateRequest.getBody());
        emailTemplate.setAccount(account);

        createEmailTemplateRequest.getTo().forEach(email ->
                emailTemplate.addRecipient(email, EmailRecipient.RecipientType.TO));
        createEmailTemplateRequest.getCc().forEach(email ->
                emailTemplate.addRecipient(email, EmailRecipient.RecipientType.CC));
        createEmailTemplateRequest.getBcc().forEach(email ->
                emailTemplate.addRecipient(email, EmailRecipient.RecipientType.BCC));

        emailTemplateRepository.save(emailTemplate);
    }

    @Transactional
    public void updateTemplate(String uid, Long templateId, UpdateEmailTemplateRequest updateEmailTemplateRequest) {
        Account account = accountRepository.findByUid(uid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCOUNT_ERROR_MESSAGE));

        EmailTemplate emailTemplate = emailTemplateRepository.findById(templateId)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_EMAIL_TEMPLATE));

        if (!emailTemplate.getAccount().equals(account)) {
            throw new CustomErrorException(ErrorCode.UNAUTHORIZED_ACCESS_TO_TEMPLATE);
        }

        emailTemplate.setTemplateName(updateEmailTemplateRequest.getTemplateName());
        emailTemplate.setSubject(updateEmailTemplateRequest.getSubject());
        emailTemplate.setBody(updateEmailTemplateRequest.getBody());

        emailTemplate.getRecipients().clear();

        updateEmailTemplateRequest.getTo().forEach(email ->
                emailTemplate.addRecipient(email, EmailRecipient.RecipientType.TO));
        updateEmailTemplateRequest.getCc().forEach(email ->
                emailTemplate.addRecipient(email, EmailRecipient.RecipientType.CC));
        updateEmailTemplateRequest.getBcc().forEach(email ->
                emailTemplate.addRecipient(email, EmailRecipient.RecipientType.BCC));

        emailTemplateRepository.save(emailTemplate);
    }

    @Transactional
    public void deleteTemplate(String uid, Long templateId) {
        Account account = accountRepository.findByUid(uid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCOUNT_ERROR_MESSAGE));

        EmailTemplate emailTemplate = emailTemplateRepository.findById(templateId)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_EMAIL_TEMPLATE));

        if (!emailTemplate.getAccount().equals(account)) {
            throw new CustomErrorException(ErrorCode.UNAUTHORIZED_ACCESS_TO_TEMPLATE);
        }

        emailTemplate.getRecipients().clear();
        emailTemplateRepository.delete(emailTemplate);
    }
}
