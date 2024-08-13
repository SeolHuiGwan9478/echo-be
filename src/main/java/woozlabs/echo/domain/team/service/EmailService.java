package woozlabs.echo.domain.team.service;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.*;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import woozlabs.echo.domain.team.dto.SendInvitationEmailDto;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final AmazonSimpleEmailService amazonSimpleEmailService;
    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    @Value("${aws.ses.from.email}")
    private String fromEmail;

    @Async
    public void sendEmailViaSMTP(SendInvitationEmailDto sendInvitationEmailDto) {
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, true, "UTF-8");
            messageHelper.setTo(sendInvitationEmailDto.getTo());
            messageHelper.setSubject("Join your team on Echo");
            messageHelper.setText(getHtmlContent(sendInvitationEmailDto), true);

            javaMailSender.send(message);
            log.info("Email sent successfully via SMTP to: {}", sendInvitationEmailDto.getTo());
        } catch (MessagingException e) {
            log.error("Failed to send email via SMTP to: {}. Error: {}", sendInvitationEmailDto.getTo(), e.getMessage());
            throw new CustomErrorException(ErrorCode.FAILED_TO_INVITATION_MAIL, e.getMessage());
        }
    }

    public void sendEmailViaSES(SendInvitationEmailDto sendInvitationEmailDto) {
        try {
            SendEmailRequest request = new SendEmailRequest()
                    .withDestination(new Destination().withToAddresses(sendInvitationEmailDto.getTo()))
                    .withMessage(new Message()
                            .withBody(new Body()
                                    .withHtml(new Content()
                                            .withCharset("UTF-8").withData(getHtmlContent(sendInvitationEmailDto))))
                            .withSubject(new Content().withCharset("UTF-8").withData("Join your team on Echo")))
                    .withSource(fromEmail);

            amazonSimpleEmailService.sendEmail(request);
            log.info("Email sent successfully via SES to: {}", sendInvitationEmailDto.getTo());
        } catch (Exception e) {
            log.error("Failed to send email via SES to: {}. Error: {}", sendInvitationEmailDto.getTo(), e.getMessage());
            throw new CustomErrorException(ErrorCode.FAILED_TO_INVITATION_MAIL, e.getMessage());
        }
    }

    private String getHtmlContent(SendInvitationEmailDto dto) {
        Context context = new Context();
        context.setVariable("username", dto.getUsername());
        context.setVariable("userImage", dto.getUserImage());
        context.setVariable("invitedByUsername", dto.getInvitedByUsername());
        context.setVariable("invitedByEmail", dto.getInvitedByEmail());
        context.setVariable("teamName", dto.getTeamName());
        context.setVariable("teamImage", dto.getTeamImage());
        context.setVariable("inviteLink", dto.getInviteLink());
        context.setVariable("inviteFromIp", dto.getInviteFromIp());
        context.setVariable("inviteFromLocation", dto.getInviteFromLocation());

        return templateEngine.process("invite-email", context);
    }
}
