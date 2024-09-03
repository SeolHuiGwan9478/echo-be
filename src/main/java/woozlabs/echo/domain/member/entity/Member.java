package woozlabs.echo.domain.member.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import woozlabs.echo.global.common.entity.BaseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Member extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String language;

    @Enumerated(EnumType.STRING)
    private Theme theme; // 'light' | 'dark' | 'system'

    private String watchNotification; // ‘INBOX’ | ‘IMPORTANT’ | string
    private boolean marketingEmails;

    @ColumnDefault("true")
    private boolean securityEmails;

    @ColumnDefault("1")
    private int density;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Account> accounts = new ArrayList<>();

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
        this.accounts.add(account);
        if (!this.accounts.contains(account)) {
            this.accounts.add(account);
        }
    }
}
