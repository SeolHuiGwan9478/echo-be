package woozlabs.echo.domain.team.entity;

import jakarta.persistence.*;
import lombok.Getter;
import woozlabs.echo.domain.member.entity.Member;

@Entity
@Getter
public class TeamMember {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @Enumerated(EnumType.STRING)
    private TeamMemberRole role;

    public enum TeamMemberRole {
        ADMIN, EDITOR, VIEWER
    }
}

