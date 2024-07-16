package woozlabs.echo.domain.gmail.util;

import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class GmailUtil {
    private final Pattern NO_REPLY_PATTERN = Pattern.compile("\\bno.*reply.*\\b", Pattern.CASE_INSENSITIVE);
    public Optional<String> extractVerification(String content, String sender){
        // check sender's email address
        if(!isVerificationEmail(sender)) return Optional.empty();
        // Decoding by using base64
        String code = "test";
        return Optional.of(code);
    }

    public String purifyEmailContent(String content){
        byte[] decodedBytes = Base64.decodeBase64(content);
        
        return "test";
    }

    private boolean isVerificationEmail(String sender){
        // Additional check on sender's email address
        Matcher matcher = NO_REPLY_PATTERN.matcher(sender);
        return matcher.find();
    }
}
