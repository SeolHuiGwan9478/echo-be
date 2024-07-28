package woozlabs.echo.domain.member.entity;

import jakarta.persistence.*;
import lombok.*;
import woozlabs.echo.domain.echo.entity.EmailTemplate;
import woozlabs.echo.domain.contactGroup.entity.MemberContactGroup;
import woozlabs.echo.domain.echo.entity.UserSidebarConfig;
import woozlabs.echo.global.common.entity.BaseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String uid;
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
    @JoinColumn(name = "super_account_id")
    private SuperAccount superAccount;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<EmailTemplate> emailTemplates = new ArrayList<>();

    @OneToOne(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private UserSidebarConfig sidebarConfig;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<MemberContactGroup> memberContactGroups = new ArrayList<>();
}
