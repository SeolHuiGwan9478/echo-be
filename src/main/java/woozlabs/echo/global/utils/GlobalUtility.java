package woozlabs.echo.global.utils;

import org.joda.time.DateTimeZone;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GlobalUtility {
    public static List<String> splitSenderData(String sender){
        List<String> splitSender = new ArrayList<>();
        String replaceSender = sender.replaceAll("[\"\\\\]", "");
        String regex = "(.*)\\s*<(.*)>";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(replaceSender);
        if(matcher.find()){
            splitSender.add(matcher.group(1).trim());
            splitSender.add(matcher.group(2).trim());
        }else{
            splitSender.add(sender.substring(0, sender.indexOf("@")));
            splitSender.add(sender);
        }
        return splitSender;
    }

    public static List<List<String>> splitCcAndBcc(String ccAndBcc){
        List<List<String>> result = new ArrayList<>();
        String replaceSender = ccAndBcc.replace("\"", "");
        String[] senders = replaceSender.split(",");
        for(String sender : senders){
            List<String> splitSender = new ArrayList<>();
            String regex = "(.*)\\s*<(.*)>";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(sender);
            if (matcher.find()) {
                splitSender.add(matcher.group(1).trim());
                splitSender.add(matcher.group(2).trim());
            } else {
                String emailRegex = "^[\\w.!#$%&'*+/=?^_`{|}~-]+@[\\w.-]+\\.[a-zA-Z]{2,}$";
                Pattern emailPattern = Pattern.compile(emailRegex);
                Matcher emailMatcher = emailPattern.matcher(ccAndBcc);
                if(emailMatcher.find()){
                    splitSender.add(sender.substring(0, sender.indexOf("@")).trim());
                    splitSender.add(sender.trim());
                }
                else{
                    splitSender.add(ccAndBcc);
                    splitSender.add(ccAndBcc);
                }
            }
            result.add(splitSender);
        }
        return result;
    }

    public static String getStandardTimeZone(String timezonePart){
        Optional<ZoneId> zoneId = Optional.empty();
        try { // Step 1. 직접 시간대로 변환 시도 Ex) "America/New_York"
            zoneId = Optional.of(ZoneId.of(timezonePart));
        } catch (Exception e) { // Step 2. 짧은 시간대로 변환 시도 Ex) "EST" -> Standard Timezone
            Map<String, String> shortIds = ZoneId.SHORT_IDS;
            String mappedZone = shortIds.get(timezonePart);
            if (mappedZone != null) {
                zoneId = Optional.of(ZoneId.of(mappedZone));
            } else { // Step 3. 모든 시간대를 확인하면서 약어 찾기 -> Non-Standard Timezone
                for (String id : ZoneId.getAvailableZoneIds()) {
                    ZoneId zone = ZoneId.of(id);
                    ZonedDateTime zdt = ZonedDateTime.now(zone);
                    if (zdt.format(DateTimeFormatter.ofPattern("z")).equals(timezonePart)) {
                        zoneId = Optional.of(zone);
                        break;
                    }
                }
            }
        }
        if (zoneId.isEmpty()) return timezonePart; // 변환 실패의 경우
        ZoneOffset offset = zoneId.get().getRules().getOffset(Instant.now());
        String offsetId = offset.getId().replace(":", "");
        if(offsetId.equals("Z")){ // GMT || UTC
            offsetId = "+0000";
        }
        return offsetId.length() == 5 ? offsetId : "+" + offsetId;
    }
}
