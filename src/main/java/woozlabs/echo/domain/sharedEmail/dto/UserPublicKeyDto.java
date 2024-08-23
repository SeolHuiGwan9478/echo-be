package woozlabs.echo.domain.sharedEmail.dto;

import woozlabs.echo.domain.sharedEmail.entity.UserPublicKey;

public class UserPublicKeyDto {

    private String uid;
    private String publicKey;

    public UserPublicKeyDto(UserPublicKey userPublicKey) {
        this.uid = userPublicKey.getUid();
        this.publicKey = userPublicKey.getPublicKey();
    }
}
