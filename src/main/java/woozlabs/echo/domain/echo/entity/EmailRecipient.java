package woozlabs.echo.domain.echo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class EmailRecipient {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email_template_id")
    private EmailTemplate emailTemplate;

    private String email;

    @Enumerated(EnumType.STRING)
    private RecipientType type;

    public enum RecipientType {
        TO, CC, BCC
    }

    public EmailRecipient(String email, RecipientType type) {
        this.email = email;
        this.type = type;
    }

    public void setEmailTemplate(EmailTemplate emailTemplate) {
        this.emailTemplate = emailTemplate;
        if (emailTemplate != null) {
            switch(this.type) {
                case TO:
                    emailTemplate.getToRecipients().add(this);
                    break;
                case CC:
                    emailTemplate.getCcRecipients().add(this);
                    break;
                case BCC:
                    emailTemplate.getBccRecipients().add(this);
                    break;
            }
        }
    }
}
