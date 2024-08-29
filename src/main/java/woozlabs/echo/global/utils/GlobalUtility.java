package woozlabs.echo.global.utils;

import org.joda.time.DateTimeZone;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GlobalUtility {
    public static List<String> splitSenderData(String sender){
        List<String> splitSender = new ArrayList<>();
        String replaceSender = sender.replace("\"", "");
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
        DateTimeZone standardTimeZone = DateTimeZone.forID(timezonePart);
        return standardTimeZone.toString();
    }
}
