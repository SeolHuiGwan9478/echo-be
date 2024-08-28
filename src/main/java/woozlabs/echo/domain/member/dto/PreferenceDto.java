package woozlabs.echo.domain.member.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PreferenceDto {

    private String lang; // 'en' or 'ko'...
    private AppearanceDto appearance;
    private NotificationDto notification;
}
