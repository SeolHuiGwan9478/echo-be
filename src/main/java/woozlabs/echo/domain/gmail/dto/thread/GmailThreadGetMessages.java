package woozlabs.echo.domain.gmail.dto.thread;

import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import woozlabs.echo.domain.gmail.util.GmailUtility;
import woozlabs.echo.global.constant.GlobalConstant;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;

import java.math.BigInteger;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.zone.ZoneRulesException;
import java.util.ArrayList;
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
    private String fromName;
    private String fromEmail;
    private List<GmailThreadGetMessagesCc> cc = new ArrayList<>();
    private List<GmailThreadGetMessagesBcc> bcc = new ArrayList<>();
    private List<GmailThreadGetMessagesTo> to = new ArrayList<>();
    private String threadId; // thread id
    private List<String> labelIds;
    private String snippet;
    private BigInteger historyId;
    private GmailThreadGetPayload payload;
    private Boolean verification = Boolean.FALSE;
    private List<String> codes;

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
                        gmailThreadGetMessages.setFromName(splitSender.get(0));
                        gmailThreadGetMessages.setFromEmail(splitSender.get(1));
                    } else {
                        gmailThreadGetMessages.setFromEmail(splitSender.get(0));
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
                            gmailThreadGetMessagesCc.setCcName(ss.get(0));
                            gmailThreadGetMessagesCc.setCcEmail(ss.get(1));
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
                            gmailThreadGetMessagesBcc.setBccName(ss.get(0));
                            gmailThreadGetMessagesBcc.setBccEmail(ss.get(1));
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
                            gmailThreadGetMessagesTo.setToName(ss.get(0));
                            gmailThreadGetMessagesTo.setToEmail(ss.get(1));
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
        gmailThreadGetMessages.setPayload(convertedPayload);
        // verification code
//        List<String> codes = findVerificationEmail(convertedPayload, gmailUtility);
//        if(!codes.isEmpty()){
//            gmailThreadGetMessages.setVerification(Boolean.TRUE);
//            gmailThreadGetMessages.setCodes(codes);
//        }
        return gmailThreadGetMessages;
    }

    private static List<String> findVerificationEmail(GmailThreadGetPayload payload, GmailUtility gmailUtility){
        // payload body check
        String payloadBody = payload.getBody().getData();
        List<String> codes = gmailUtility.extractVerification(payloadBody);
        List<GmailThreadGetPart> parts = payload.getParts();
        for(GmailThreadGetPart part : parts){
            findVerificationCode(part, codes, gmailUtility);
        }
        return codes.stream().distinct().toList();
    }

    private static void findVerificationCode(GmailThreadGetPart inputPart, List<String> codes, GmailUtility gmailUtility){
        List<GmailThreadGetPart> parts = inputPart.getParts();
        if(parts.isEmpty()){
            String partBody = inputPart.getBody().getData();
            codes.addAll(gmailUtility.extractVerification(partBody));
            return;
        }
        for(GmailThreadGetPart part : parts){
            findVerificationCode(part, codes, gmailUtility);
        }
        String partBody = inputPart.getBody().getData();
        codes.addAll(gmailUtility.extractVerification(partBody));
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
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern(
                    INPUT_GMAIL_DATE_FORMAT, Locale.ENGLISH
            );
            LocalDateTime convertDate = LocalDateTime.parse(datePart, inputFormatter);
            if (timezonePart.matches(EXTRA_TIMEZONE_PATTERN)) {
                timezonePart = timezonePart.substring(0, 5);
            } // separate parts
            ZoneId losAngelesZone = ZoneId.of("America/Los_Angeles");
            // 원본 시간대 처리
            ZoneId originalZone;
            if (timezonePart.startsWith(GMT)) {
                originalZone = ZoneId.of(timezonePart.replace(GMT, "Z"));
            } else {
                originalZone = ZoneId.of(timezonePart);
            }

            // 원본 시간을 Los Angeles 시간대로 변환
            ZonedDateTime losAngelesTime = convertDate.atZone(originalZone)
                    .withZoneSameInstant(losAngelesZone);

            DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
            String finalDate = losAngelesTime.format(outputFormatter);
            gmailThreadGetMessages.setDate(finalDate);
            // Los Angeles 시간대 이름 설정
            String timezoneName = losAngelesZone.getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            gmailThreadGetMessages.setTimezone(timezoneName);
        } else {
            gmailThreadGetMessages.setDate(originDate);
            //throw new CustomErrorException(ErrorCode.FAILED_TO_CHANGE_DATE_FORMAT);
        }
    }
}