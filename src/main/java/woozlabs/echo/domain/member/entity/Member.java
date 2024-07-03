package woozlabs.echo.domain.member.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import woozlabs.echo.global.common.entity.BaseEntity;

@Entity
@Getter
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

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToOne(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private SuperAccount superAccount;

    @OneToOne(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private SubAccount subAccount;
}
