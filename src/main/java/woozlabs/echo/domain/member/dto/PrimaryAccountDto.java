package woozlabs.echo.domain.member.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrimaryAccountDto {

    private MemberDto member;
    private AccountDto account;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberDto {
        private Long id;
        private String displayName;
        private String memberName;
        private String profileImageUrl;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountDto {
        private String id;
        private String email;
        private String providerName;
        private String profileImageUrl;
    }
}
