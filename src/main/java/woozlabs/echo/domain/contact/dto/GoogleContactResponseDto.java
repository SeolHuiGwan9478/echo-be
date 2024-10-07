package woozlabs.echo.domain.contact.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoogleContactResponseDto {

    private String email;
    private Name names;
    private String profileImageUrl;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Name {
        private String displayName;
        private String familyName;
        private String givenName;
    }
}
