package woozlabs.echo.domain.member.dto;

import lombok.Builder;
import lombok.Data;
import woozlabs.echo.domain.member.entity.Theme;

@Data
@Builder
public class AppearanceDto {
    private Theme theme; // 'light' | 'dark' | 'system'
}

