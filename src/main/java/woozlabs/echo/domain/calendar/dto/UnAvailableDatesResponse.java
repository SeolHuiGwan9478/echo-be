package woozlabs.echo.domain.calendar.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UnAvailableDatesResponse {
    private List<String> unavailableDates;
}
