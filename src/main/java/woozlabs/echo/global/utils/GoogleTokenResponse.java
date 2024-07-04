package woozlabs.echo.global.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class GoogleTokenResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("expires_in")
    private Long expiresIn;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("id_token")
    private String idToken;

    @JsonProperty("scope")
    private String scope;

    @JsonProperty("token_type")
    private String tokenType;
}
