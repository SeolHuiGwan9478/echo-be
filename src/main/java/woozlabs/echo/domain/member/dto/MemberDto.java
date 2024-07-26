package woozlabs.echo.domain.member.dto;


import lombok.*;
import woozlabs.echo.domain.member.entity.Member;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDto {

    private String uid;
    private String displayName;
    private String email;
    private String profileImageUrl;
    private boolean isPrimary;
    private List<LinkedAccountDto> linkedAccounts;

    public MemberDto(Member member, List<LinkedAccountDto> linkedAccounts) {
        this.uid = member.getUid();
        this.displayName = member.getDisplayName();
        this.email = member.getEmail();
        this.profileImageUrl = member.getProfileImageUrl();
        this.isPrimary = member.isPrimary();
        this.linkedAccounts = linkedAccounts;
    }
}
