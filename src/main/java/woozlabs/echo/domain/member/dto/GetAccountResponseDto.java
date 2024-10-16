package woozlabs.echo.domain.member.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetAccountResponseDto {

    private List<AccountDto> accounts;
    private List<RelatedMemberDto> relatedMembers;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountDto {
        private String uid;
        private String email;
        private String displayName;
        private String profileImageUrl;
        private String provider;
        private List<String> scopes;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RelatedMemberDto {
        private String displayName;
        private String memberName;
        private String email;
        private String primaryUid;
        private String profileImageUrl;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime createdAt;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime updatedAt;
    }
}
