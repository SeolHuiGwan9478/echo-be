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
import woozlabs.echo.domain.member.entity.Member;
import woozlabs.echo.domain.member.repository.MemberRepository;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EmailTemplateService {

    private final EmailTemplateRepository emailTemplateRepository;
    private final MemberRepository memberRepository;
    public List<EmailTemplateResponse> getAllTemplates(String uid) {
        Member member = memberRepository.findByUid(uid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER_ERROR_MESSAGE));

        List<EmailTemplate> emailTemplates = emailTemplateRepository.findByMember(member);
        return emailTemplates.stream()
                .map(EmailTemplateResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void createTemplate(String uid, CreateEmailTemplateRequest createEmailTemplateRequest) {
        Member member = memberRepository.findByUid(uid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER_ERROR_MESSAGE));

        EmailTemplate emailTemplate = new EmailTemplate();
        emailTemplate.setTemplateName(createEmailTemplateRequest.getTemplateName());
        emailTemplate.setSubject(createEmailTemplateRequest.getSubject());
        emailTemplate.setBody(createEmailTemplateRequest.getBody());
        emailTemplate.setMember(member);

        List<EmailRecipient> recipients = createEmailTemplateRequest.getTo().stream()
                        .map(email -> {
                            EmailRecipient recipient = new EmailRecipient();
                            recipient.setEmail(email);
                            recipient.setEmailTemplate(emailTemplate);
                            return recipient;
                        })
                        .collect(Collectors.toList());
        emailTemplate.setRecipients(recipients);

        emailTemplateRepository.save(emailTemplate);
    }

    @Transactional
    public void updateTemplate(String uid, Long templateId, UpdateEmailTemplateRequest updateEmailTemplateRequest) {
        Member member = memberRepository.findByUid(uid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER_ERROR_MESSAGE));

        EmailTemplate emailTemplate = emailTemplateRepository.findById(templateId)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_EMAIL_TEMPLATE));

        if (!emailTemplate.getMember().equals(member)) {
            throw new CustomErrorException(ErrorCode.UNAUTHORIZED_ACCESS_TO_TEMPLATE);
        }

        emailTemplate.setTemplateName(updateEmailTemplateRequest.getTemplateName());
        emailTemplate.setSubject(updateEmailTemplateRequest.getSubject());
        emailTemplate.setBody(updateEmailTemplateRequest.getBody());

        // Clear existing recipients and update with new ones
        emailTemplate.getRecipients().clear();
        List<EmailRecipient> recipients = updateEmailTemplateRequest.getTo().stream()
                .map(email -> {
                    EmailRecipient recipient = new EmailRecipient();
                    recipient.setEmail(email);
                    recipient.setEmailTemplate(emailTemplate);
                    return recipient;
                })
                .collect(Collectors.toList());
        emailTemplate.getRecipients().addAll(recipients);

        emailTemplateRepository.save(emailTemplate);
    }

    @Transactional
    public void deleteTemplate(String uid, Long templateId) {
        Member member = memberRepository.findByUid(uid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER_ERROR_MESSAGE));

        EmailTemplate emailTemplate = emailTemplateRepository.findById(templateId)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_EMAIL_TEMPLATE));

        if (!emailTemplate.getMember().equals(member)) {
            throw new CustomErrorException(ErrorCode.UNAUTHORIZED_ACCESS_TO_TEMPLATE);
        }

        emailTemplate.getRecipients().clear();
        emailTemplateRepository.delete(emailTemplate);
    }
}
