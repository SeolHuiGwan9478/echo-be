package woozlabs.echo.global.utils;

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
}
