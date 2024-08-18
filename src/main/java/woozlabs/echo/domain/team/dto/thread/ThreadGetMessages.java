package woozlabs.echo.domain.team.dto.thread;

import lombok.Getter;
import lombok.NoArgsConstructor;
import woozlabs.echo.domain.gmail.dto.thread.GmailThreadGetMessagesFrom;
import woozlabs.echo.domain.gmail.dto.verification.ExtractVerificationInfo;

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
    private ExtractVerificationInfo verification = new ExtractVerificationInfo();
}
