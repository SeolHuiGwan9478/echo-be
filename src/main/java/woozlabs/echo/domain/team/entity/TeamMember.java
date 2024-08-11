package woozlabs.echo.domain.team.entity;

import jakarta.persistence.*;
import lombok.*;
import woozlabs.echo.domain.member.entity.Member;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamMember {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Builder
    public TeamMember(Team team, Member member, Role role) {
        this.team = team;
        this.member = member;
        this.role = role;
    }
}

