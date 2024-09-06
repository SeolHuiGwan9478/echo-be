package woozlabs.echo.domain.sharedEmail.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendSharedEmailInvitationDto {

    private String invitationMemo;
    private String access;
    private String permission;
    private String dataId;
    private String sharedDataType;
}
