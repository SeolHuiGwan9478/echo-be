package woozlabs.echo.domain.gmail.dto.thread;

import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
import lombok.Data;
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
import static woozlabs.echo.global.utils.GlobalUtility.splitSenderData;

@Data
public class GmailThreadGetMessages {
    private String id; // message id
    private String date;
    private String timezone; // timezone
    private String fromName;
    private String fromEmail;
    private List<String> ccNames;
    private List<String> ccEmails;
    private List<String> bccNames;
    private List<String> bccEmails;
    private List<String> toNames;
    private List<String> toEmails;
    private String threadId; // thread id
    private List<String> labelIds;
    private String snippet;
    private BigInteger historyId;
    private GmailThreadGetPayload payload;

    public static GmailThreadGetMessages toGmailThreadGetMessages(Message message){
        GmailThreadGetMessages gmailThreadGetMessages = new GmailThreadGetMessages();
        MessagePart payload = message.getPayload();
        GmailThreadGetPayload convertedPayload = new GmailThreadGetPayload(payload);
        List<MessagePartHeader> headers = payload.getHeaders(); // parsing header
        List<String> ccNames = new ArrayList<>();
        List<String> ccEmails = new ArrayList<>();
        List<String> bccNames = new ArrayList<>();
        List<String> bccEmails = new ArrayList<>();
        List<String> toNames = new ArrayList<>();
        List<String> toEmails = new ArrayList<>();
        for(MessagePartHeader header: headers) {
            switch (header.getName()) {
                case THREAD_PAYLOAD_HEADER_FROM_KEY -> {
                    String sender = header.getValue();
                    List<String> splitSender = splitSenderData(sender);
                    if (splitSender.size() != 1){
                        gmailThreadGetMessages.setFromName(splitSender.get(0));
                        gmailThreadGetMessages.setFromEmail(splitSender.get(1));
                    }
                    else{
                        gmailThreadGetMessages.setFromEmail(splitSender.get(0));
                    }
                }
                case THREAD_PAYLOAD_HEADER_DATE_KEY -> {
                    String originDate = header.getValue();
                    changeDateFormat(originDate, gmailThreadGetMessages);
                }
                case THREAD_PAYLOAD_HEADER_CC_KEY -> {
                    String oneCc = header.getValue();
                    List<String> splitSender = splitSenderData(oneCc);
                    if(!ccNames.contains(splitSender.get(0))){
                        ccNames.add(splitSender.get(0));
                        ccEmails.add(splitSender.get(1));
                    }
                }
                case THREAD_PAYLOAD_HEADER_BCC_KEY -> {
                    String oneBcc = header.getValue();
                    List<String> splitSender = splitSenderData(oneBcc);
                    if(!bccNames.contains(splitSender.get(0))){
                        bccNames.add(splitSender.get(0));
                        bccEmails.add(splitSender.get(1));
                    }
                }
                case THREAD_PAYLOAD_HEADER_TO_KEY -> {
                    String oneTo = header.getValue();
                    List<String> splitSender = splitSenderData(oneTo);
                    if(!toNames.contains(splitSender.get(0))){
                        toNames.add(splitSender.get(0));
                        toEmails.add(splitSender.get(1));
                    }
                }
            }
        }
        gmailThreadGetMessages.setId(message.getId());
        gmailThreadGetMessages.setThreadId(message.getThreadId());
        gmailThreadGetMessages.setLabelIds(message.getLabelIds());
        gmailThreadGetMessages.setPayload(convertedPayload);
        gmailThreadGetMessages.setCcNames(ccNames);
        gmailThreadGetMessages.setCcEmails(ccEmails);
        gmailThreadGetMessages.setBccNames(bccNames);
        gmailThreadGetMessages.setBccEmails(bccEmails);
        gmailThreadGetMessages.setToNames(toNames);
        gmailThreadGetMessages.setToEmails(toEmails);
        return gmailThreadGetMessages;
    }

    private static void changeDateFormat(String originDate, GmailThreadGetMessages gmailThreadGetMessages) {
        Pattern pattern = Pattern.compile(
                DATE_TIMEZONE_PATTERN
        );
        Matcher matcher = pattern.matcher(originDate);
        if (matcher.matches()) {
            String datePart = matcher.group("date");
            String timezonePart = matcher.group("timezone");

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
            String timezoneName = losAngelesZone.getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            gmailThreadGetMessages.setTimezone(timezoneName);
        } else {
            throw new CustomErrorException(ErrorCode.FAILED_TO_CHANGE_DATE_FORMAT);
        }
    }
}