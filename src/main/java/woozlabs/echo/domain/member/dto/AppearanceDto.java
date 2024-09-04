package woozlabs.echo.domain.member.dto;

import lombok.*;
import woozlabs.echo.domain.member.entity.Density;
import woozlabs.echo.domain.member.entity.Theme;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppearanceDto {

    private Theme theme; // 'light' | 'dark' | 'system'
    private Density density; // 'compact' | 'cozy'
}

