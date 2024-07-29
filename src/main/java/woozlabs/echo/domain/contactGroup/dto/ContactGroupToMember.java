package woozlabs.echo.domain.contactGroup.dto;

import lombok.*;
import woozlabs.echo.domain.member.entity.Member;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactGroupToMember {

    private String uid;
    private String displayName;
    private String email;
    private String profileImageUrl;

    public ContactGroupToMember(Member member) {
        this.uid = member.getUid();
        this.displayName = member.getDisplayName();
        this.email = member.getEmail();
        this.profileImageUrl = member.getProfileImageUrl();
    }
}
