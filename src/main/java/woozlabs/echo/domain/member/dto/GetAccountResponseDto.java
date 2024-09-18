package woozlabs.echo.domain.member.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetAccountResponseDto {

    private AccountDto account;
    private List<PrimaryAccountDto> primaryAccounts;

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
        private String providerId;
    }
}
