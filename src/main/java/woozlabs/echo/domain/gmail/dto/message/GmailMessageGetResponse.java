package woozlabs.echo.domain.gmail.dto.message;

import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
import lombok.Data;
import woozlabs.echo.domain.gmail.dto.extract.ExtractVerificationInfo;
import woozlabs.echo.domain.gmail.dto.thread.GmailThreadGetMessagesFrom;
import woozlabs.echo.domain.gmail.util.GmailUtility;
import woozlabs.echo.global.dto.ResponseDto;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.utils.GlobalUtility;

import java.math.BigInteger;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.zone.ZoneRules;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static woozlabs.echo.global.constant.GlobalConstant.*;
import static woozlabs.echo.global.constant.GlobalConstant.TIMEZONE_PATTERN_2;
import static woozlabs.echo.global.utils.GlobalUtility.splitCcAndBcc;
import static woozlabs.echo.global.utils.GlobalUtility.splitSenderData;

@Data
public class GmailMessageGetResponse implements ResponseDto {
    private String id; // message id
    private String subject;
    private String date;
    private String timezone; // timezone
    private GmailMessageGetFrom from;
    private List<GmailMessageGetCc> cc = new ArrayList<>();
    private List<GmailMessageGetBcc> bcc = new ArrayList<>();
    private List<GmailMessageGetTo> to = new ArrayList<>();
    private String threadId; // thread id
    private List<String> labelIds;
    private String snippet;
    private BigInteger historyId;
    private GmailMessageGetPayload payload;
    private ExtractVerificationInfo verification = new ExtractVerificationInfo();
    public static GmailMessageGetResponse toGmailMessageGet(Message message, GmailUtility gmailUtility){
        GmailMessageGetResponse gmailMessageGetResponse = new GmailMessageGetResponse();
        MessagePart payload = message.getPayload();
        GmailMessageGetPayload convertedPayload = new GmailMessageGetPayload(payload);
        List<MessagePartHeader> headers = payload.getHeaders(); // parsing header
        for(MessagePartHeader header: headers) {
            switch (header.getName()) {
                case MESSAGE_PAYLOAD_HEADER_FROM_KEY -> {
                    String sender = header.getValue();
                    List<String> splitSender = splitSenderData(sender);
                    if (splitSender.size() == 2) {
                        gmailMessageGetResponse.setFrom(GmailMessageGetFrom.builder()
                                .name(splitSender.get(0))
                                .email(splitSender.get(1))
                                .build()
                        );
                    } else {
                        gmailMessageGetResponse.setFrom(GmailMessageGetFrom.builder()
                                .name(header.getValue())
                                .email(header.getValue())
                                .build()
                        );
                    }
                }case MESSAGE_PAYLOAD_HEADER_CC_KEY -> {
                    String oneCc = header.getValue();
                    List<List<String>> splitSender = splitCcAndBcc(oneCc);
                    if (!splitSender.isEmpty()){
                        List<GmailMessageGetCc> data = splitSender.stream().map((ss) -> {
                            GmailMessageGetCc gmailMessageGetCc = new GmailMessageGetCc();
                            gmailMessageGetCc.setName(ss.get(0));
                            gmailMessageGetCc.setEmail(ss.get(1));
                            return gmailMessageGetCc;
                        }).toList();
                        gmailMessageGetResponse.setCc(data);
                    }
                }case MESSAGE_PAYLOAD_HEADER_BCC_KEY -> {
                    String oneBcc = header.getValue();
                    List<List<String>> splitSender = splitCcAndBcc(oneBcc);
                    if(!splitSender.isEmpty()){
                        List<GmailMessageGetBcc> data = splitSender.stream().map((ss) -> {
                            GmailMessageGetBcc gmailMessageGetBcc = new GmailMessageGetBcc();
                            gmailMessageGetBcc.setName(ss.get(0));
                            gmailMessageGetBcc.setEmail(ss.get(1));
                            return gmailMessageGetBcc;
                        }).toList();
                        gmailMessageGetResponse.setBcc(data);
                    }
                }case MESSAGE_PAYLOAD_HEADER_TO_KEY -> {
                    String oneTo = header.getValue();
                    List<List<String>> splitSender = splitCcAndBcc(oneTo);
                    if(!splitSender.isEmpty()){
                        List<GmailMessageGetTo> data = splitSender.stream().map((ss) -> {
                            GmailMessageGetTo gmailMessageGetTo = new GmailMessageGetTo();
                            gmailMessageGetTo.setName(ss.get(0));
                            gmailMessageGetTo.setEmail(ss.get(1));
                            return gmailMessageGetTo;
                        }).toList();
                        gmailMessageGetResponse.setTo(data);
                    }
                }case MESSAGE_PAYLOAD_HEADER_SUBJECT_KEY -> {
                    String subject = header.getValue();
                    gmailMessageGetResponse.setSubject(subject);
                }
//                }case MESSAGE_PAYLOAD_HEADER_DATE_KEY -> {
//                    String date = header.getValue();
//                    extractAndSetDateTime(date, gmailMessageGetResponse);
//                }
            }
        }
        gmailMessageGetResponse.setDate(message.getInternalDate().toString());
        gmailMessageGetResponse.setId(message.getId());
        gmailMessageGetResponse.setThreadId(message.getThreadId());
        gmailMessageGetResponse.setLabelIds(message.getLabelIds());
        gmailMessageGetResponse.setSnippet(message.getSnippet());
        gmailMessageGetResponse.setHistoryId(message.getHistoryId());
        gmailMessageGetResponse.setPayload(convertedPayload);
        // verification code
        ExtractVerificationInfo verificationInfo = findVerificationEmail(convertedPayload, gmailUtility);
        if(!verificationInfo.getCodes().isEmpty() || !verificationInfo.getLinks().isEmpty()){
            verificationInfo.setVerification(Boolean.TRUE);
        }
        gmailMessageGetResponse.setVerification(verificationInfo);
        return gmailMessageGetResponse;
    }



    private static void extractAndSetDateTime(String date, GmailMessageGetResponse gmailMessageGetResponse) {
        List<Pattern> patterns = List.of(
                Pattern.compile("([+-]\\d{4})$"), // +0900
                Pattern.compile("\\(([A-Z]{3,4})\\)$"), //(KST)
                Pattern.compile("([A-Z]{3,4})$") // KST
        );
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(date);
            if (matcher.find()) {
                String timezonePart = matcher.group(1);
                if(!pattern.pattern().equals(Pattern.compile("([+-]\\d{4})$").pattern())){
                    timezonePart = GlobalUtility.getStandardTimeZone(timezonePart);
                    ZoneId zone = ZoneId.of(timezonePart);
                    ZoneOffset offset = zone.getRules().getOffset(Instant.now());
                    timezonePart = offset.toString().replaceAll(":", "");
                }
                convertToIanaTimezone(gmailMessageGetResponse, timezonePart);
                break;
            }
        }
    }

    private static ExtractVerificationInfo findVerificationEmail(GmailMessageGetPayload payload, GmailUtility gmailUtility){
        // payload body check
        String payloadBody = payload.getBody().getData();
        ExtractVerificationInfo verificationInfo;
        if(payload.getMimeType().equals("text/html")) verificationInfo = gmailUtility.extractVerification(payloadBody);
        else verificationInfo = new ExtractVerificationInfo();
        List<GmailMessageGetPart> parts = payload.getParts();
        for(GmailMessageGetPart part : parts){
            if(part.getMimeType().equals("text/html")) findVerificationInfoInParts(part, verificationInfo, gmailUtility);
        }
        return verificationInfo;
    }

    private static void findVerificationInfoInParts(GmailMessageGetPart inputPart, ExtractVerificationInfo info, GmailUtility gmailUtility){
        List<GmailMessageGetPart> parts = inputPart.getParts();
        if(parts.isEmpty()){
            String partBody = inputPart.getBody().getData();
            ExtractVerificationInfo newInfo = gmailUtility.extractVerification(partBody);
            info.updateCodes(newInfo.getCodes());
            info.updateLinks(newInfo.getLinks());
            return;
        }
        for(GmailMessageGetPart part : parts){
            findVerificationInfoInParts(part, info, gmailUtility);
        }
        String partBody = inputPart.getBody().getData();
        ExtractVerificationInfo newInfo = gmailUtility.extractVerification(partBody);
        info.updateCodes(newInfo.getCodes());
        info.updateLinks(newInfo.getLinks());
    }

    private static void convertToIanaTimezone(GmailMessageGetResponse gmailMessageGetResponse, String timezonePart) {
        try {
            ZoneOffset offset = ZoneOffset.of(timezonePart);
            for (String zoneId : ZoneOffset.getAvailableZoneIds()) {
                ZoneId zone = ZoneId.of(zoneId);
                if (zone.getRules().getOffset(Instant.now()).equals(offset)) {
                    gmailMessageGetResponse.setTimezone(zoneId);
                    break;
                }
            }
        } catch (Exception e) {
            gmailMessageGetResponse.setTimezone(null);
        }
    }
}