package woozlabs.echo.domain.echo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import woozlabs.echo.domain.echo.dto.emailTemplate.CreateEmailTemplateRequest;
import woozlabs.echo.domain.echo.dto.emailTemplate.EmailTemplateResponse;
import woozlabs.echo.domain.echo.dto.emailTemplate.UpdateEmailTemplateRequest;
import woozlabs.echo.domain.echo.entity.EmailRecipient;
import woozlabs.echo.domain.echo.entity.EmailTemplate;
import woozlabs.echo.domain.echo.repository.EmailTemplateRepository;
import woozlabs.echo.domain.member.entity.Member;
import woozlabs.echo.domain.member.repository.MemberRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailTemplateServiceTest {

    // Mock 객체 생성
    @Mock
    private EmailTemplateRepository emailTemplateRepository;

    // Mock 객체 생성
    @Mock
    private MemberRepository memberRepository;

    // 생성한 위의 Mock 객체들을 EmailTemplateService에 주입
    @InjectMocks
    private EmailTemplateService emailTemplateService;

    private Member member;
    private EmailTemplate emailTemplate;

    @BeforeEach
    void setUp() {
        member = new Member();
        member.setId(1L);
        member.setUid("1234567891");

        emailTemplate = new EmailTemplate();
        emailTemplate.setId(1L);
        emailTemplate.setTemplateName("Template1");
        emailTemplate.setSubject("Subject1");
        emailTemplate.setBody("Body1");
        emailTemplate.setMember(member);
    }

    @Test
    public void getAllTemplatesTest() throws Exception {
        // given
        List<EmailTemplate> templates = new ArrayList<>();
        templates.add(emailTemplate);

        // when
        when(memberRepository.findByUid("1234567891")).thenReturn(Optional.of(member));
        when(emailTemplateRepository.findByMember(member)).thenReturn(templates);

        // then
        List<EmailTemplateResponse> responses = emailTemplateService.getAllTemplates("1234567891");

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Template1", responses.get(0).getTemplateName());
        assertEquals("Subject1", responses.get(0).getSubject());
        assertEquals("Body1", responses.get(0).getBody());
        assertEquals(1L, responses.get(0).getKey());
    }

    @Test
    public void createTemplateTest() throws Exception {
        // given
        CreateEmailTemplateRequest request = new CreateEmailTemplateRequest();
        request.setTemplateName("New Template");
        request.setSubject("New Subject");
        List<String> recipients = Arrays.asList("test1@example.com", "test2@example.com");
        request.setTo(recipients);
        request.setBody("New Body");

        when(memberRepository.findByUid("1234567891")).thenReturn(Optional.of(member));

        EmailTemplate newTemplate = new EmailTemplate();
        newTemplate.setId(2L);
        newTemplate.setTemplateName(request.getTemplateName());
        newTemplate.setSubject(request.getSubject());
        newTemplate.setBody(request.getBody());
        newTemplate.setMember(member);
        newTemplate.setRecipients(recipients.stream()
                .map(email -> {
                    EmailRecipient recipient = new EmailRecipient();
                    recipient.setEmail(email);
                    return recipient;
                }).collect(Collectors.toList()));

        // when
        when(emailTemplateRepository.save(argThat(template ->
                template.getTemplateName().equals("New Template") &&
                template.getSubject().equals("New Subject") &&
                template.getBody().equals("New Body") &&
                template.getMember().equals(member) &&
                template.getRecipients().size() == 2 &&
                template.getRecipients().get(0).getEmail().equals("test1@example.com") &&
                template.getRecipients().get(1).getEmail().equals("test2@example.com")
        ))).thenReturn(newTemplate);


        emailTemplateService.createTemplate("1234567891", request);

        // then
        verify(emailTemplateRepository, times(1)).save(argThat(template ->
                template.getTemplateName().equals("New Template") &&
                template.getSubject().equals("New Subject") &&
                template.getBody().equals("New Body") &&
                template.getMember().equals(member) &&
                template.getRecipients().size() == 2 &&
                template.getRecipients().get(0).getEmail().equals("test1@example.com") &&
                template.getRecipients().get(1).getEmail().equals("test2@example.com")
        ));
    }

    @Test
    public void updateTemplateTest() throws Exception {
        // given
        UpdateEmailTemplateRequest request = new UpdateEmailTemplateRequest();
        request.setTemplateName("Updated Template");
        request.setSubject("Updated Subject");
        List<String> recipients = Arrays.asList("test1@example.com");
        request.setTo(recipients);
        request.setBody("Updated Body");

        EmailTemplate existingTemplate = new EmailTemplate();
        existingTemplate.setId(3L);
        existingTemplate.setTemplateName("Old Template");
        existingTemplate.setSubject("Old Subject");
        existingTemplate.setBody("Old Body");
        existingTemplate.setMember(member);

        EmailTemplate updatedTemplate = new EmailTemplate();
        updatedTemplate.setId(3L);
        updatedTemplate.setTemplateName(request.getTemplateName());
        updatedTemplate.setSubject(request.getSubject());
        updatedTemplate.setBody(request.getBody());
        updatedTemplate.setMember(member);
        updatedTemplate.setRecipients(recipients.stream()
                .map(email -> {
                    EmailRecipient recipient = new EmailRecipient();
                    recipient.setEmail(email);
                    return recipient;
                }).collect(Collectors.toList()));

        // when
        when(memberRepository.findByUid("1234567891")).thenReturn(Optional.of(member));
        when(emailTemplateRepository.findById(3L)).thenReturn(Optional.of(existingTemplate));
        when(emailTemplateRepository.save(argThat(template ->
                template.getTemplateName().equals("Updated Template") &&
                        template.getSubject().equals("Updated Subject") &&
                        template.getBody().equals("Updated Body") &&
                        template.getMember().equals(member) &&
                        template.getRecipients().size() == 1 &&
                        template.getRecipients().get(0).getEmail().equals("test1@example.com")
        ))).thenReturn(updatedTemplate);

        emailTemplateService.updateTemplate("1234567891", 3L, request);

        // then
        verify(emailTemplateRepository, times(1)).save(argThat(template ->
                template.getTemplateName().equals("Updated Template") &&
                template.getSubject().equals("Updated Subject") &&
                template.getBody().equals("Updated Body") &&
                template.getMember().equals(member) &&
                template.getRecipients().size() == 1 &&
                template.getRecipients().get(0).getEmail().equals("test1@example.com")
        ));
    }

    @Test
    public void deleteTemplateTest() throws Exception {
        // given
        Long templateId = 4L;

        // 변경을 위한 이미 존재하는 emailTemplate 생성
        EmailTemplate existingTemplate = new EmailTemplate();
        existingTemplate.setId(templateId); // templateId와 동일하게 설정
        existingTemplate.setTemplateName("Old Template");
        existingTemplate.setSubject("Old Subject");
        existingTemplate.setBody("Old Body");
        existingTemplate.setMember(member);

        when(memberRepository.findByUid("1234567891")).thenReturn(Optional.of(member));
        when(emailTemplateRepository.findById(templateId)).thenReturn(Optional.of(existingTemplate));

        // when
        emailTemplateService.deleteTemplate("1234567891", templateId);

        // then
        verify(emailTemplateRepository, times(1)).delete(existingTemplate);
    }
}