package woozlabs.echo.domain.member.dto;

import lombok.*;
import woozlabs.echo.domain.member.entity.Member;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkedAccountDto {

    private String uid;
    private String displayName;
    private String email;
    private String profileImageUrl;
    private boolean isPrimary;

    public LinkedAccountDto(Member member) {
        this.uid = member.getUid();
        this.displayName = member.getDisplayName();
        this.email = member.getEmail();
        this.profileImageUrl = member.getProfileImageUrl();
        this.isPrimary = member.isPrimary();
    }
}
