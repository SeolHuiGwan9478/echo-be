package woozlabs.echo.domain.gmail.dto.thread;

import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import woozlabs.echo.domain.gmail.dto.extract.ExtractVerificationInfo;
import woozlabs.echo.domain.gmail.dto.message.GmailMessageGetResponse;
import woozlabs.echo.domain.gmail.util.GmailUtility;
import woozlabs.echo.global.utils.GlobalUtility;

import java.math.BigInteger;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static woozlabs.echo.global.constant.GlobalConstant.*;
import static woozlabs.echo.global.utils.GlobalUtility.splitCcAndBcc;
import static woozlabs.echo.global.utils.GlobalUtility.splitSenderData;

@Slf4j
@Data
public class GmailThreadGetMessagesResponse {
    private String id; // message id
    private Long timestamp;
    private String timezone = ""; // timezone
    private GmailThreadGetMessagesFrom from;
    private List<GmailThreadGetMessagesCc> cc = new ArrayList<>();
    private List<GmailThreadGetMessagesBcc> bcc = new ArrayList<>();
    private List<GmailThreadGetMessagesTo> to = new ArrayList<>();
    private String threadId; // thread id
    private List<String> labelIds;
    private String snippet;
    private BigInteger historyId;
    private GmailThreadGetPayload payload;
    private ExtractVerificationInfo verification = new ExtractVerificationInfo();

    public static GmailThreadGetMessagesResponse toGmailThreadGetMessages(Message message, GmailUtility gmailUtility){
        GmailThreadGetMessagesResponse gmailThreadGetMessages = new GmailThreadGetMessagesResponse();
        MessagePart payload = message.getPayload();
        GmailThreadGetPayload convertedPayload = new GmailThreadGetPayload(payload);
        List<MessagePartHeader> headers = payload.getHeaders(); // parsing header
        if(message.getSnippet().contains("Hello Kwanwoo, Thank you for considering our homes at 340 Fremont")){
            log.info(headers.toString());
        }
        for(MessagePartHeader header: headers) {
            switch (header.getName()) {
                case THREAD_PAYLOAD_HEADER_FROM_KEY -> {
                    String sender = header.getValue();
                    List<String> splitSender = splitSenderData(sender);
                    if (splitSender.size() == 2) {
                        gmailThreadGetMessages.setFrom(GmailThreadGetMessagesFrom.builder()
                                .name(splitSender.get(0))
                                .email(splitSender.get(1))
                                .build()
                        );
                    } else {
                        gmailThreadGetMessages.setFrom(GmailThreadGetMessagesFrom.builder()
                                .name(header.getValue())
                                .email(header.getValue())
                                .build()
                        );
                    }
                }case THREAD_PAYLOAD_HEADER_CC_KEY -> {
                    String oneCc = header.getValue();
                    List<List<String>> splitSender = splitCcAndBcc(oneCc);
                    if (!splitSender.isEmpty()){
                        List<GmailThreadGetMessagesCc> data = splitSender.stream().map((ss) -> {
                            GmailThreadGetMessagesCc gmailThreadGetMessagesCc = new GmailThreadGetMessagesCc();
                            gmailThreadGetMessagesCc.setName(ss.get(0));
                            gmailThreadGetMessagesCc.setEmail(ss.get(1));
                            return gmailThreadGetMessagesCc;
                        }).toList();
                        gmailThreadGetMessages.setCc(data);
                    }
                }case THREAD_PAYLOAD_HEADER_BCC_KEY -> {
                    String oneBcc = header.getValue();
                    List<List<String>> splitSender = splitCcAndBcc(oneBcc);
                    if(!splitSender.isEmpty()){
                        List<GmailThreadGetMessagesBcc> data = splitSender.stream().map((ss) -> {
                            GmailThreadGetMessagesBcc gmailThreadGetMessagesBcc = new GmailThreadGetMessagesBcc();
                            gmailThreadGetMessagesBcc.setName(ss.get(0));
                            gmailThreadGetMessagesBcc.setEmail(ss.get(1));
                            return gmailThreadGetMessagesBcc;
                        }).toList();
                        gmailThreadGetMessages.setBcc(data);
                    }
                }case THREAD_PAYLOAD_HEADER_TO_KEY -> {
                    String oneTo = header.getValue();
                    List<List<String>> splitSender = splitCcAndBcc(oneTo);
                    if (!splitSender.isEmpty()) {
                        List<GmailThreadGetMessagesTo> data = splitSender.stream().map((ss) -> {
                            GmailThreadGetMessagesTo gmailThreadGetMessagesTo = new GmailThreadGetMessagesTo();
                            gmailThreadGetMessagesTo.setName(ss.get(0));
                            gmailThreadGetMessagesTo.setEmail(ss.get(1));
                            return gmailThreadGetMessagesTo;
                        }).toList();
                        gmailThreadGetMessages.setTo(data);
                    }
                }case MESSAGE_PAYLOAD_HEADER_DATE_KEY -> {
                    String timestamp = header.getValue();
                    extractAndSetDateTime(timestamp, gmailThreadGetMessages);
                }
            }
        }
        gmailThreadGetMessages.setTimestamp(message.getInternalDate());
        gmailThreadGetMessages.setId(message.getId());
        gmailThreadGetMessages.setThreadId(message.getThreadId());
        gmailThreadGetMessages.setLabelIds(message.getLabelIds());
        gmailThreadGetMessages.setSnippet(message.getSnippet());
        gmailThreadGetMessages.setHistoryId(message.getHistoryId());
        gmailThreadGetMessages.setPayload(convertedPayload);
        return gmailThreadGetMessages;
    }

    private static void extractAndSetDateTime(String date, GmailThreadGetMessagesResponse gmailThreadGetMessages) {
        List<Pattern> patterns = List.of(
                Pattern.compile("([+-]\\d{4})$"),
                Pattern.compile("\\(([A-Z]{3,4})\\)$"),
                Pattern.compile("([A-Z]{3,4})$")
        );
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(date);
            if (matcher.find()) {
                String timezonePart = matcher.group(1);
                if(!pattern.pattern().equals(Pattern.compile("([+-]\\d{4})$").pattern())){
                    timezonePart = GlobalUtility.getStandardTimeZone(timezonePart);
                }
                convertToIanaTimezone(gmailThreadGetMessages, timezonePart);
                break;
            }
        }
    }

    private static void convertToIanaTimezone(GmailThreadGetMessagesResponse gmailThreadGetMessages, String timezonePart) {
        try {
            ZoneOffset offset = ZoneOffset.of(timezonePart);
            for (String zoneId : ZoneOffset.getAvailableZoneIds()) {
                ZoneId zone = ZoneId.of(zoneId);
                if (zone.getRules().getOffset(Instant.now()).equals(offset)) {
                    gmailThreadGetMessages.setTimezone(zoneId);
                    break;
                }
            }
        } catch (Exception e) {
            gmailThreadGetMessages.setTimezone(null);
        }
    }
}