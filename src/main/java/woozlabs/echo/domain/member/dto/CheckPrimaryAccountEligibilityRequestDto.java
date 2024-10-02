package woozlabs.echo.domain.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CheckPrimaryAccountEligibilityRequestDto {
    private List<String> uids;
}
