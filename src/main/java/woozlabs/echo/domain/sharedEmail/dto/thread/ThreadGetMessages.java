package woozlabs.echo.domain.sharedEmail.dto.thread;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class ThreadGetMessages {

    private String id; // message id
    private String date;
    private String timezone; // timezone
    private ThreadGetMessagesFrom from;
    private List<ThreadGetMessagesCc> cc = new ArrayList<>();
    private List<ThreadGetMessagesBcc> bcc = new ArrayList<>();
    private List<ThreadGetMessagesTo> to = new ArrayList<>();
    private String threadId; // thread id
    private List<String> labelIds;
    private String snippet;
    private BigInteger historyId;
    private ThreadGetPayload payload;
    private ThreadExtractVerificationInfo verification = new ThreadExtractVerificationInfo();
}
