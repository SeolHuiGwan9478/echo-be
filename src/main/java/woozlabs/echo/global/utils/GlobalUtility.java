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
            String emailRegex = "^([^@]+)@[^@]+$";
            // Compile the regular expression into a pattern
            Pattern emailpattern = Pattern.compile(emailRegex);
            // Match the pattern against the email
            Matcher matcherEmail = emailpattern.matcher(sender);
            splitSender.add(matcherEmail.group(1));
            splitSender.add(sender);
        }
        return splitSender;
    }
}
