package woozlabs.echo.domain.member.dto;


import lombok.*;
import woozlabs.echo.domain.member.entity.Account;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {

    private String uid;
    private String displayName;
    private String email;
    private String profileImageUrl;
    private String language;
    private boolean isPrimary;
    private List<LinkedAccountDto> linkedAccounts;

    public AccountDto(Account account, List<LinkedAccountDto> linkedAccounts) {
        this.uid = account.getUid();
        this.displayName = account.getDisplayName();
        this.email = account.getEmail();
        this.profileImageUrl = account.getProfileImageUrl();
        this.isPrimary = account.isPrimary();
        this.linkedAccounts = linkedAccounts;
    }
}
