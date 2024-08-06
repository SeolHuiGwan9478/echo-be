package woozlabs.echo.domain.team.entity;

import jakarta.persistence.*;
import lombok.Getter;
import woozlabs.echo.domain.member.entity.Member;

import java.time.LocalDateTime;

@Entity
@Getter
public class TeamInvitation {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inviter_id")
    private Member inviter;

    private String inviteeEmail;

    @Enumerated(EnumType.STRING)
    private InvitationStatus status;

    private String token;
    private LocalDateTime expiresAt;
    private LocalDateTime sentAt;

    public enum InvitationStatus {
        PENDING, ACCEPTED, REJECTED, EXPIRED
    }

    public enum InviteeRole {
        ADMIN, EDITOR, VIEWER
    }
}
