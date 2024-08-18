package woozlabs.echo.domain.team.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import woozlabs.echo.domain.member.entity.Member;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    @Enumerated(EnumType.STRING)
    private Role role;

    private String token;
    private LocalDateTime expiresAt;
    private LocalDateTime sentAt;
    private boolean deleted = Boolean.FALSE;

    public enum InvitationStatus {
        PENDING, ACCEPTED, REJECTED, EXPIRED
    }

    public void accept() {
        this.status = InvitationStatus.ACCEPTED;
    }

    public void softDelete() {
        this.deleted = true;
    }

    public void setStatus(InvitationStatus status) {
        this.status = status;
    }

    @Builder
    public TeamInvitation(Team team, Member inviter, String inviteeEmail, String token, LocalDateTime expiresAt, LocalDateTime sentAt, Role inviteeRole) {
        this.team = team;
        this.inviter = inviter;
        this.inviteeEmail = inviteeEmail;
        this.status = InvitationStatus.PENDING;
        this.token = token;
        this.expiresAt = expiresAt;
        this.sentAt = sentAt;
        this.role = inviteeRole;
    }
}
