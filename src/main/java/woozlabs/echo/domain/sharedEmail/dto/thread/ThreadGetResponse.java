package woozlabs.echo.domain.sharedEmail.dto.thread;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.List;

@Getter
@NoArgsConstructor
public class ThreadGetResponse {

    private String id;
    private BigInteger historyId;
    private List<ThreadGetMessages> messages;
}
