package woozlabs.echo.domain.team.dto;

import woozlabs.echo.domain.team.entity.UserPublicKey;

public class UserPublicKeyDto {

    private String uid;
    private String publicKey;

    public UserPublicKeyDto(UserPublicKey userPublicKey) {
        this.uid = userPublicKey.getUid();
        this.publicKey = userPublicKey.getPublicKey();
    }
}
