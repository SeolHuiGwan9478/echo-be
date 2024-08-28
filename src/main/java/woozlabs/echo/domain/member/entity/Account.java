package woozlabs.echo.domain.member.entity;

import jakarta.persistence.*;
import lombok.*;
import woozlabs.echo.domain.echo.entity.EmailTemplate;
import woozlabs.echo.domain.contactGroup.entity.AccountContactGroup;
import woozlabs.echo.domain.echo.entity.UserSidebarConfig;
import woozlabs.echo.domain.signature.Signature;
import woozlabs.echo.global.common.entity.BaseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String uid;
    private String provider;
    private String googleProviderId;
    private String displayName;
    private String email;
    private String profileImageUrl;
    private String accessToken;
    private String refreshToken;
    private LocalDateTime accessTokenFetchedAt;
    private boolean isPrimary;

    @Enumerated(EnumType.STRING)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    private List<EmailTemplate> emailTemplates = new ArrayList<>();

    @OneToOne(mappedBy = "account", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private UserSidebarConfig sidebarConfig;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    private List<AccountContactGroup> accountContactGroups = new ArrayList<>();

    @OneToMany(mappedBy = "ownerId", cascade = CascadeType.ALL)
    private List<Signature> allSignatures = new ArrayList<>();

    public List<Signature> getSignatures() {
        return allSignatures.stream()
                .filter(signature -> signature.getType() == Signature.SignatureType.MEMBER)
                .collect(Collectors.toList());
    }
}
