package woozlabs.echo.domain.calendar.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CalendarListResponse {
    private int totalCounts;
    private List<CalendarListData> data;
}
