package woozlabs.echo.domain.member.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetAccountResponseDto {

    private List<AccountDto> accounts;
    private List<RelatedAccountDto> relatedAccounts;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountDto {
        private Long id;
        private String uid;
        private String email;
        private String displayName;
        private String profileImageUrl;
        private String provider;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RelatedAccountDto {
        private MemberDto member;
        private AccountDto account;
    }

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
}
