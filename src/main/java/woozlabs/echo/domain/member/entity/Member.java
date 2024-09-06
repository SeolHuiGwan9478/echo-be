package woozlabs.echo.domain.member.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import woozlabs.echo.global.common.entity.BaseEntity;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Member extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String language = "en";

    @Enumerated(EnumType.STRING)
    private Theme theme = Theme.LIGHT;

    private String watchNotification;
    private boolean marketingEmails;

    @ColumnDefault("true")
    private boolean securityEmails = true;

    @Enumerated(EnumType.STRING)
    private Density density = Density.COMPACT;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Account> accounts = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "member_account_emails", joinColumns = @JoinColumn(name = "member_id"))
    @Column(name = "email")
    private Set<String> accountEmails = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "member_term_agreements", joinColumns = @JoinColumn(name = "member_id"))
    @MapKeyColumn(name = "agreement_type")
    @Column(name = "timestamp")
    private Map<String, LocalDateTime> termAgreements = new HashMap<>();

    @ElementCollection
    @CollectionTable(name = "member_marketing_agreements", joinColumns = @JoinColumn(name = "member_id"))
    @MapKeyColumn(name = "agreement_type")
    @Column(name = "timestamp")
    private Map<String, LocalDateTime> marketingAgreements = new HashMap<>();

    public void addAccount(Account account) {
        if (!this.accounts.contains(account)) {
            this.accounts.add(account);
            this.accountEmails.add(account.getEmail());
        }
    }
}
