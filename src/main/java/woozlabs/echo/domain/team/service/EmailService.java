package woozlabs.echo.domain.team.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import woozlabs.echo.domain.team.dto.SendInvitationEmailDto;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    public void sendInvitationEmail(SendInvitationEmailDto sendInvitationEmailDto) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(sendInvitationEmailDto.getTo());
        message.setSubject(sendInvitationEmailDto.getInviterName() + "님이 " + sendInvitationEmailDto.getTeamName() + " 팀에 초대했습니다.");
        message.setText("팀에 참여하려면 다음 링크를 클릭하세요: " + sendInvitationEmailDto.getInvitationLink());
        javaMailSender.send(message);
    }
}
