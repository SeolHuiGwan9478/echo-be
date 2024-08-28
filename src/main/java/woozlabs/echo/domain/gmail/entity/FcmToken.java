package woozlabs.echo.domain.gmail.entity;

import jakarta.persistence.*;
import lombok.*;
import woozlabs.echo.domain.member.entity.Account;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FcmToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    private String fcmToken;
    private String machineUuid;

    public void updateFcmToken(String newFcmToken){
        this.fcmToken = newFcmToken;
    }
}
