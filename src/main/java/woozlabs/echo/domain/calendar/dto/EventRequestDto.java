package woozlabs.echo.domain.calendar.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRequestDto {

    private String summary;
    private String location;
    private String description;
    private String startDateTime; // ISO 8601 형식의 문자열 (예: 2024-07-08T10:00:00)
    private String endDateTime; // ISO 8601 형식의 문자열 (예: 2024-07-08T11:00:00)
    private String timeZone;
    private boolean createGoogleMeet;
}
