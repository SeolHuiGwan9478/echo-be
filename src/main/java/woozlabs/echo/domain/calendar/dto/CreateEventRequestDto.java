package woozlabs.echo.domain.calendar.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateEventRequestDto {

    private String summary;
    private String location;
    private String description;
    private String startDateTime; // 문자열로 전달될 예정
    private String endDateTime; // 문자열로 전달될 예정
}
