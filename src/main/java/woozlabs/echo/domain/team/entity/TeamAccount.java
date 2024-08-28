package woozlabs.echo.domain.team.entity;

import jakarta.persistence.*;
import lombok.*;
import woozlabs.echo.domain.member.entity.Account;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamAccount {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Builder
    public TeamAccount(Team team, Account account, Role role) {
        this.team = team;
        this.account = account;
        this.role = role;
    }
}

