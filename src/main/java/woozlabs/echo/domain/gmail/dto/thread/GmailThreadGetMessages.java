package woozlabs.echo.domain.gmail.dto.thread;

import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import woozlabs.echo.domain.gmail.dto.verification.ExtractVerificationInfo;
import woozlabs.echo.domain.gmail.util.GmailUtility;
import woozlabs.echo.global.constant.GlobalConstant;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.zone.ZoneRulesException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static woozlabs.echo.global.constant.GlobalConstant.*;
import static woozlabs.echo.global.utils.GlobalUtility.splitCcAndBcc;
import static woozlabs.echo.global.utils.GlobalUtility.splitSenderData;

@Data
public class GmailThreadGetMessages {
    private String id; // message id
    private String date;
    private String timezone; // timezone
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

    public static GmailThreadGetMessages toGmailThreadGetMessages(Message message, GmailUtility gmailUtility){
        GmailThreadGetMessages gmailThreadGetMessages = new GmailThreadGetMessages();
        MessagePart payload = message.getPayload();
        GmailThreadGetPayload convertedPayload = new GmailThreadGetPayload(payload);
        List<MessagePartHeader> headers = payload.getHeaders(); // parsing header
        for(MessagePartHeader header: headers) {
            switch (header.getName()) {
                case THREAD_PAYLOAD_HEADER_FROM_KEY -> {
                    String sender = header.getValue();
                    List<String> splitSender = splitSenderData(sender);
                    if (splitSender.size() != 1) {
                        gmailThreadGetMessages.setFrom(GmailThreadGetMessagesFrom.builder()
                                .name(splitSender.get(0))
                                .email(splitSender.get(1))
                                .build()
                        );
                    } else {
                        gmailThreadGetMessages.setFrom(GmailThreadGetMessagesFrom.builder()
                                .email(splitSender.get(0))
                                .build()
                        );
                    }
                }case THREAD_PAYLOAD_HEADER_DATE_KEY -> {
                    String originDate = header.getValue();
                    changeDateFormat(originDate, gmailThreadGetMessages);
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
                    if(!splitSender.isEmpty()){
                        List<GmailThreadGetMessagesTo> data = splitSender.stream().map((ss) -> {
                            GmailThreadGetMessagesTo gmailThreadGetMessagesTo = new GmailThreadGetMessagesTo();
                            gmailThreadGetMessagesTo.setName(ss.get(0));
                            gmailThreadGetMessagesTo.setEmail(ss.get(1));
                            return gmailThreadGetMessagesTo;
                        }).toList();
                        gmailThreadGetMessages.setTo(data);
                    }
                }
            }
        }
        gmailThreadGetMessages.setId(message.getId());
        gmailThreadGetMessages.setThreadId(message.getThreadId());
        gmailThreadGetMessages.setLabelIds(message.getLabelIds());
        gmailThreadGetMessages.setSnippet(message.getSnippet());
        gmailThreadGetMessages.setHistoryId(message.getHistoryId());
        gmailThreadGetMessages.setPayload(convertedPayload);
        // verification code
//        ExtractVerificationInfo verificationInfo = findVerificationEmail(convertedPayload, gmailUtility);
//        if(!verificationInfo.getCodes().isEmpty() || !verificationInfo.getLinks().isEmpty()){
//            verificationInfo.setVerification(Boolean.TRUE);
//        }
//        gmailThreadGetMessages.setVerification(verificationInfo);
        return gmailThreadGetMessages;
    }

    private static ExtractVerificationInfo findVerificationEmail(GmailThreadGetPayload payload, GmailUtility gmailUtility){
        // payload body check
        String payloadBody = payload.getBody().getData();
        ExtractVerificationInfo verificationInfo;
        if(payload.getMimeType().equals("text/html")) verificationInfo = gmailUtility.extractVerification(payloadBody);
        else verificationInfo = new ExtractVerificationInfo();
        List<GmailThreadGetPart> parts = payload.getParts();
        for(GmailThreadGetPart part : parts){
            if(part.getMimeType().equals("text/html")) findVerificationInfoInParts(part, verificationInfo, gmailUtility);
        }
        return verificationInfo;
    }

    private static void findVerificationInfoInParts(GmailThreadGetPart inputPart, ExtractVerificationInfo info, GmailUtility gmailUtility){
        List<GmailThreadGetPart> parts = inputPart.getParts();
        if(parts.isEmpty()){
            String partBody = inputPart.getBody().getData();
            ExtractVerificationInfo newInfo = gmailUtility.extractVerification(partBody);
            info.updateCodes(newInfo.getCodes());
            info.updateLinks(newInfo.getLinks());
            return;
        }
        for(GmailThreadGetPart part : parts){
            findVerificationInfoInParts(part, info, gmailUtility);
        }
        String partBody = inputPart.getBody().getData();
        ExtractVerificationInfo newInfo = gmailUtility.extractVerification(partBody);
        info.updateCodes(newInfo.getCodes());
        info.updateLinks(newInfo.getLinks());
    }

    private static void changeDateFormat(String originDate, GmailThreadGetMessages gmailThreadGetMessages) {
        Pattern pattern = Pattern.compile(
                DATE_TIMEZONE_PATTERN
        );
        Matcher matcher = pattern.matcher(originDate);
        if (matcher.matches()) {
            String datePart = matcher.group(1);
            String timezonePart = matcher.group(2);
            datePart = datePart.replaceAll("\\s+", " ");
            // parsing dateTime
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern(
                    INPUT_GMAIL_DATE_FORMAT, Locale.ENGLISH
            );
            LocalDateTime dateTime = LocalDateTime.parse(datePart, inputFormatter);
            // converting timezone format
            if (timezonePart.matches(TIMEZONE_PATTERN_1)) {
                timezonePart = timezonePart.substring(0, 5);
                convertToIanaTimezone(gmailThreadGetMessages, timezonePart);
            }else if(timezonePart.matches(TIMEZONE_PATTERN_2)){
                convertToIanaTimezone(gmailThreadGetMessages, timezonePart);
            }else{
                ZoneId zone = ZoneId.of(timezonePart);
                ZoneOffset offset = zone.getRules().getOffset(java.time.Instant.now());
                timezonePart = offset.toString().replaceAll(":", "");
                gmailThreadGetMessages.setTimezone(timezonePart);
            }
            ZoneId zoneId = ZoneId.of(timezonePart);
            ZonedDateTime zonedDateTime = dateTime.atZone(zoneId);
            DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
            gmailThreadGetMessages.setDate(zonedDateTime.format(outputFormatter));
        } else {
            gmailThreadGetMessages.setDate(originDate);
        }
    }

    private static void convertToIanaTimezone(GmailThreadGetMessages gmailThreadGetMessages, String timezonePart) {
        try{
            ZoneOffset offset = ZoneOffset.of(timezonePart);
            for (String zoneId : ZoneOffset.getAvailableZoneIds()){
                ZoneId zone = ZoneId.of(zoneId);
                if(zone.getRules().getOffset(Instant.now()).equals(offset)){
                    gmailThreadGetMessages.setTimezone(zoneId);
                    break;
                }
            }
        }catch (Exception e){
            gmailThreadGetMessages.setTimezone(null);
        }
    }
}