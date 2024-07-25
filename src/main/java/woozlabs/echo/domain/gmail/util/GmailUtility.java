package woozlabs.echo.domain.gmail.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class GmailUtility {
    public Optional<String> extractVerification(String content, String sender){
        // check sender's email address
        if(!isVerificationEmail(sender)) return Optional.empty();
        // Decoding by using base64
        String code = "test";
        return Optional.of(code);
    }

    private boolean isVerificationEmail(String rawContent){
        // Additional check on sender's email address
        byte[] decodedBinaryContent = Base64.getDecoder().decode(rawContent);
        String decodedContent = new String(decodedBinaryContent, StandardCharsets.UTF_8);
        Document doc = Jsoup.parse(decodedContent);
        String bodyText = doc.body().text().toLowerCase();
        List<String> englishKeywords = readKeywords("keywords_en.txt");
        List<String> koreanKeywords = readKeywords("keywords_ko.txt");
        for(String englishKeyword : englishKeywords){
            if(bodyText.contains(englishKeyword)) return true;
        }
        for(String koreanKeyword : koreanKeywords){
            if(bodyText.contains(koreanKeyword)) return true;
        }
        return false;
    }

    private List<String> readKeywords(String fileName){
        List<String> keywords = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                keywords.addAll(Arrays.asList(line.split(",")));
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return keywords;
    }
}
