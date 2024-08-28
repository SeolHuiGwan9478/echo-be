package woozlabs.echo.domain.contactGroup.dto;

import lombok.*;
import woozlabs.echo.domain.member.entity.Account;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactGroupToAccount {

    private String uid;
    private String displayName;
    private String email;
    private String profileImageUrl;

    public ContactGroupToAccount(Account account) {
        this.uid = account.getUid();
        this.displayName = account.getDisplayName();
        this.email = account.getEmail();
        this.profileImageUrl = account.getProfileImageUrl();
    }
}
