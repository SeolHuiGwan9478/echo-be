package woozlabs.echo.domain.gmail.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class GmailUtility {
    public Optional<String> extractVerification(String rawContent){
        byte[] decodedBinaryContent = Base64.getDecoder().decode(rawContent);
        String decodedContent = new String(decodedBinaryContent, StandardCharsets.UTF_8);
        if(!isVerificationEmail(decodedContent)) return Optional.empty(); // check verification email
        // Decoding by using base64
        String code = "test";
        return Optional.of(code);
    }

//    private List<String> extractVerificationCode(String decodedContent){
//        Document doc = Jsoup.parse(decodedContent);
//        List<String> codes = new ArrayList<>();
//        List<String> englishKeywords = readKeywords("src/main/resources/keywords_en.txt");
//        List<String> koreanKeywords = readKeywords("src/main/resources/keywords_ko.txt");
//        for(String englishKeyword : englishKeywords){
//            Elements elements = doc.getElementsContainingText(englishKeyword);
//            for(Element element : elements){
//                Element previousElement = element.previousElementSibling();
//                Element nextElement = element.nextElementSibling();
//                Element parentElement = element.parent();
//                if(previousElement != null){
//
//                }
//                if(nextElement != null){
//
//                }
//                if(parentElement != null){
//
//                }
//            }
//        }
//        for(String koreanKeyword : koreanKeywords){
//
//        }
//    }

//    private String extractVerificationCode(String text){
//        List<String> patterns = List.of("\\b\\d{6}\\b", "\\b[A-Z0-9]{6,}\\b");
//        for(String pattern : patterns){
//            Pattern regexPattern = Pattern.compile(pattern);
//            Matcher matcher = regexPattern.matcher(text);
//
//        }
//    }

    private boolean isVerificationEmail(String decodedContent){
        Document doc = Jsoup.parse(decodedContent);
        String bodyText = doc.body().text().toLowerCase();
        List<String> englishKeywords = readKeywords("src/main/resources/keywords_en.txt");
        List<String> koreanKeywords = readKeywords("src/main/resources/keywords_ko.txt");
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
