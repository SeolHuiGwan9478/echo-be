package woozlabs.echo.domain.sharedEmail.dto.thread;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class ThreadExtractVerificationInfo {

    private Boolean verification = Boolean.FALSE;
    private List<String> codes = new ArrayList<>();
    private List<String> links = new ArrayList<>();
}
