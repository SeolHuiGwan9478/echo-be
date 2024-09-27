package woozlabs.echo.domain.member.dto.profile;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeProfileRequestDto {

    private String displayName;
    private String profileImageUrl;
    private String language;
}
