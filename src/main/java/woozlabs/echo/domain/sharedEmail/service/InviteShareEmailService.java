package woozlabs.echo.domain.sharedEmail.service;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import woozlabs.echo.domain.sharedEmail.dto.SendSharedEmailInvitationDto;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;

@Slf4j
@Service
@RequiredArgsConstructor
public class InviteShareEmailService {

    private final AmazonSimpleEmailService amazonSimpleEmailService;
    private final TemplateEngine templateEngine;

    @Value("${aws.ses.from.email}")
    private String fromEmail;

    @Async
    public void sendEmailViaSES(String inviteeEmail, String invitationMemo, SendSharedEmailInvitationDto emailContentDto) {
        try {
            SendEmailRequest request = new SendEmailRequest()
                    .withDestination(new Destination().withToAddresses(inviteeEmail))
                    .withMessage(new Message()
                            .withBody(new Body()
                                    .withHtml(new Content()
                                            .withCharset("UTF-8").withData(getHtmlContent(invitationMemo, emailContentDto))))
                            .withSubject(new Content().withCharset("UTF-8").withData("Join your team on Echo")))
                    .withSource(fromEmail);

            amazonSimpleEmailService.sendEmail(request);
            log.info("Email sent successfully via SES to: {}", inviteeEmail);
        } catch (Exception e) {
            log.error("Failed to send email via SES to: {}. Error: {}", inviteeEmail, e.getMessage());
            throw new CustomErrorException(ErrorCode.FAILED_TO_INVITATION_MAIL, e.getMessage());
        }
    }

    private String getHtmlContent(String invitationMemo, SendSharedEmailInvitationDto sendSharedEmailInvitationDto) {
        Context context = new Context();
        context.setVariable("invitationMemo", sendSharedEmailInvitationDto.getInvitationMemo());
        context.setVariable("access", sendSharedEmailInvitationDto.getAccess());
        context.setVariable("permission", sendSharedEmailInvitationDto.getPermission());
        context.setVariable("dataId", sendSharedEmailInvitationDto.getDataId());
        context.setVariable("sharedDataType", sendSharedEmailInvitationDto.getSharedDataType());

        return templateEngine.process("public-shared-email", context);
    }

}