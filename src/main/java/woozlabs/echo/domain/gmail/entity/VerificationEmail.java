package woozlabs.echo.domain.gmail.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import woozlabs.echo.domain.member.entity.Account;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class VerificationEmail {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String threadId;
    private String messageId;
    private String codes;
    private String links;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Account account;
}