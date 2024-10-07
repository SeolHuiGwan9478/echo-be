package woozlabs.echo.domain.member.entity;

import jakarta.persistence.*;
import lombok.*;
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
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String displayName;
    private String email;
    private String memberName;
    private String profileImageUrl;
    private String primaryUid;
    private String language = "en";

    @Enumerated(EnumType.STRING)
    private Theme theme = Theme.LIGHT;

    private boolean marketingEmails;
    private LocalDateTime deletedAt;

    @ColumnDefault("true")
    private boolean securityEmails = true;

    @Enumerated(EnumType.STRING)
    private Density density = Density.COMPACT;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberAccount> memberAccounts = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "accounts_watch_notifications", joinColumns = @JoinColumn(name = "member_id"))
    @MapKeyColumn(name = "account_uid")
    @Column(name = "notification_preference")
    private Map<String, Watch> watchNotifications = new HashMap<>();

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

    public void addMemberAccount(MemberAccount memberAccount) {
        this.memberAccounts.add(memberAccount);
        memberAccount.setMember(this);
    }
}
