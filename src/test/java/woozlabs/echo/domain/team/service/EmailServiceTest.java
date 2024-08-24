package woozlabs.echo.domain.team.service;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.amazonaws.services.simpleemail.model.SendEmailResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.TemplateEngine;
import woozlabs.echo.domain.team.dto.SendInvitationEmailDto;
import woozlabs.echo.domain.team.entity.TeamMemberRole;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @InjectMocks
    private EmailService emailService;

    @Mock
    private AmazonSimpleEmailService amazonSimpleEmailService;

    @Mock
    private TemplateEngine templateEngine;

    private SendInvitationEmailDto sendInvitationEmailDto;

    @BeforeEach
    void setUp() {
        sendInvitationEmailDto = SendInvitationEmailDto.builder()
                .to("echo@example.com")
                .username("echo")
                .userImage("https://vercel.com/static/vercel-user.png")
                .invitedByUsername("Admin")
                .invitedByEmail("jh07050@gmail.com")
                .teamName("woozlabs")
                .teamTeamMemberRole(TeamMemberRole.ADMIN)
                .teamImage("https://vercel.com/static/vercel-team.png")
                .inviteLink("https://example.com/invite")
                .build();

        when(templateEngine.process(anyString(), any())).thenReturn("<html>Mocked HTML content</html>");
    }

    @Test
    @DisplayName("AWS SES를 통해 팀 초대 메일을 발송합니다.")
    void sendEmailViaSES() throws Exception {
        // given
        SendEmailResult sendEmailResult = new SendEmailResult();
        when(amazonSimpleEmailService.sendEmail(any(SendEmailRequest.class))).thenReturn(sendEmailResult);

        long startTime = System.currentTimeMillis();

        // when
        emailService.sendEmailViaSES(sendInvitationEmailDto);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // then
        System.out.println("SES Email Send Duration: " + duration + " ms");

        // 검증
        ArgumentCaptor<SendEmailRequest> captor = ArgumentCaptor.forClass(SendEmailRequest.class);
        verify(amazonSimpleEmailService, times(1)).sendEmail(captor.capture());
        SendEmailRequest capturedRequest = captor.getValue();

        assertNotNull(capturedRequest, "SendEmailRequest should not be null");
        assertEquals("echo@example.com", capturedRequest.getDestination().getToAddresses().get(0));
        assertEquals("Join your team on Echo", capturedRequest.getMessage().getSubject().getData());
    }
}
