package woozlabs.echo.domain.member.dto.profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountProfileResponseDto {

    private String uid;
    private String provider;
    private String displayName;
    private String profileImageUrl;
    private String email;
    private boolean isPrimary;
}
