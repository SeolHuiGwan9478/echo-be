package woozlabs.echo.domain.member.dto;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreferenceDto {

    private String lang; // 'en' or 'ko'...
    private AppearanceDto appearance;
    private NotificationDto notification;
}
