package woozlabs.echo.domain.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoogleResponseDto {

    private String displayName;
    private String email;
    private String profileImageUrl;
    private String customToken;
    private String providerId;
    private String googleIdToken;
    private String googleAccessToken;
    private String googleRefreshToken;
}
