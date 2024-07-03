package woozlabs.echo.domain.member.entity;

import jakarta.persistence.*;
import lombok.*;
import woozlabs.echo.global.common.entity.BaseEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "super_account")
public class SuperAccount extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String uid;
    private String googleProviderId;
    private String displayName;
    private String email;
    private String profileImageUrl;
    private String accessToken;
    private String refreshToken;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "superAccount", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubAccount> subAccounts = new ArrayList<>();
}
