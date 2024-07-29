package woozlabs.echo.domain.gmail.dto.thread;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GmailThreadListThreadsFrom {
    private List<String> fromNames;
    private List<String> fromEmails;
}
