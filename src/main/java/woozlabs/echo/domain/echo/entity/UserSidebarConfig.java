package woozlabs.echo.domain.echo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import woozlabs.echo.domain.member.entity.Member;
import woozlabs.echo.domain.member.entity.SuperAccount;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class UserSidebarConfig {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", unique = true)
    private Member member;

    @Column(columnDefinition = "TEXT")
    private String sidebarConfig;
}
