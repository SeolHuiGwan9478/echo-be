package woozlabs.echo.domain.calendar.dto;

import com.google.api.services.calendar.model.CalendarListEntry;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CalendarListData {
    private String id;
    private String etag;
    private String summary;
    private String description;
    private String location;
    private String timeZone;
    private String accessRole;
    private String foregroundColor;
    private String backgroundColor;
    // later add

    public static CalendarListData toCalendarListData(CalendarListEntry calendarListEntry){
        return CalendarListData.builder()
                .id(calendarListEntry.getId())
                .etag(calendarListEntry.getEtag())
                .summary(calendarListEntry.getSummary())
                .description(calendarListEntry.getDescription())
                .location(calendarListEntry.getLocation())
                .timeZone(calendarListEntry.getTimeZone())
                .accessRole(calendarListEntry.getAccessRole())
                .foregroundColor(calendarListEntry.getForegroundColor())
                .backgroundColor(calendarListEntry.getBackgroundColor())
                .build();
    }
}
