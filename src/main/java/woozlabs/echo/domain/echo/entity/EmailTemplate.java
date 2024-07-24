package woozlabs.echo.domain.echo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import woozlabs.echo.domain.member.entity.Member;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class EmailTemplate {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String templateName;
    private String subject;

    @OneToMany(mappedBy = "emailTemplate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmailRecipient> recipients = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String body;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    public List<EmailRecipient> getToRecipients() {
        return recipients.stream()
                .filter(r -> r.getType() == EmailRecipient.RecipientType.TO)
                .collect(Collectors.toList());
    }

    public List<EmailRecipient> getCcRecipients() {
        return recipients.stream()
                .filter(r -> r.getType() == EmailRecipient.RecipientType.CC)
                .collect(Collectors.toList());
    }

    public List<EmailRecipient> getBccRecipients() {
        return recipients.stream()
                .filter(r -> r.getType() == EmailRecipient.RecipientType.BCC)
                .collect(Collectors.toList());
    }

    public void addRecipient(String email, EmailRecipient.RecipientType type) {
        EmailRecipient recipient = new EmailRecipient(email, type);
        recipient.setEmailTemplate(this);
        this.recipients.add(recipient);
    }
}
