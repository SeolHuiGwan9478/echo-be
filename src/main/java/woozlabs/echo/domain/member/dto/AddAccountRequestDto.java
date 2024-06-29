package woozlabs.echo.domain.member.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddAccountRequestDto {

    private String uid;
    private String displayName;
    private String email;
    private boolean emailVerified;
    private String photoURL;
    private String googleAccessToken;
    private String refreshToken;
}
