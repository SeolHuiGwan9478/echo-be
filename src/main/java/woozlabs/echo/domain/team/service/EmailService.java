package woozlabs.echo.domain.team.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import woozlabs.echo.domain.team.dto.SendInvitationEmailDto;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    @Async
    public void sendInvitationEmail(SendInvitationEmailDto sendInvitationEmailDto) {
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, true, "UTF-8");
            messageHelper.setTo(sendInvitationEmailDto.getTo());
            messageHelper.setSubject(sendInvitationEmailDto.getInvitedByUsername() + "님이 나를 " +
                    sendInvitationEmailDto.getTeamName() + " 워크스페이스에 초대했습니다");
            messageHelper.setText(getHtmlContent(sendInvitationEmailDto), true);

            javaMailSender.send(message);
        } catch (MessagingException e) {
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
