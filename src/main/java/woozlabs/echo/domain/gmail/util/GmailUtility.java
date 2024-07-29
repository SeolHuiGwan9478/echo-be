package woozlabs.echo.domain.gmail.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import woozlabs.echo.domain.gmail.dto.thread.GmailThreadGetBody;
import woozlabs.echo.domain.gmail.dto.thread.GmailThreadGetPart;
import woozlabs.echo.domain.gmail.dto.thread.GmailThreadGetPayload;
import woozlabs.echo.domain.gmail.dto.verification.ExtractVerificationInfo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class GmailUtility {
    public ExtractVerificationInfo extractVerification(String rawContent){
        List<String> codes = new ArrayList<>();
        List<String> links = new ArrayList<>();
        ExtractVerificationInfo extractVerificationInfo = new ExtractVerificationInfo();
        if(rawContent == null){
            return extractVerificationInfo;
        }
        // Convert URL-safe Base64 to standard Base64
        String standardBase64 = rawContent.replace('-', '+').replace('_', '/');
        // Add padding if necessary
        int paddingCount = (4 - (standardBase64.length() % 4)) % 4;
        for (int i = 0; i < paddingCount; i++) {
            standardBase64 += "=";
        }
        byte[] decodedBinaryContent = Base64.getDecoder().decode(standardBase64);
        String decodedContent = new String(decodedBinaryContent, StandardCharsets.UTF_8);
        if(!isVerificationEmail(decodedContent)) return extractVerificationInfo; // check verification email
        links.addAll(getVerificationLink(decodedContent));
        codes.addAll(getVerificationCode(decodedContent));
        if(!codes.isEmpty() || !links.isEmpty()){
            extractVerificationInfo.setVerification(Boolean.TRUE);
        }
        extractVerificationInfo.setLinks(links);
        extractVerificationInfo.setCodes(codes);
        return extractVerificationInfo;
    }

    private List<String> getVerificationLink(String decodedContent){
        Document doc = Jsoup.parse(decodedContent);
        List<String> links = new ArrayList<>();
        List<String> keywords = readKeywords("src/main/resources/keywords_en.txt");
        keywords.addAll(readKeywords("src/main/resources/keywords_ko.txt"));
        for(String keyword : keywords){
            List<Element> elements = doc.getAllElements().stream().filter((element) -> element.ownText().toLowerCase().contains(keyword)).toList();
            if(elements.isEmpty()) continue;
            for(Element element : elements){
                links.addAll(extractVerificationLink(element));
            }
        }
        return links.stream().distinct().toList();
    }

    private List<String> getVerificationCode(String decodedContent){
        Document doc = Jsoup.parse(decodedContent);
        List<String> codes = new ArrayList<>();
        List<String> keywords = readKeywords("src/main/resources/keywords_en.txt");
        keywords.addAll(readKeywords("src/main/resources/keywords_ko.txt"));
        for(String keyword : keywords){
            List<Element> elements = doc.getAllElements().stream().filter((element) -> element.ownText().toLowerCase().contains(keyword)).toList();
            for(Element element : elements){
                Element previousElement = element.previousElementSibling(); // 이전 태그
                Element nextElement = element.nextElementSibling();
                Element parentElement = element.parent();
                if(previousElement != null){
                    codes.addAll(extractVerificationCode(previousElement.text()));
                }
                if(nextElement != null){
                    codes.addAll(extractVerificationCode(nextElement.text()));
                }
                if(parentElement != null){
                    codes.addAll(extractVerificationCode(parentElement.text()));
                }
            }
        }
        codes = codes.stream().distinct().toList();
        return codes;
    }

    private List<String> extractVerificationCode(String text){
        List<String> patterns = List.of(
                "\\b\\d{6}\\b", // common code
                "\\b[a-z]{5}-[a-z]{4}-[a-z]{5}-[a-z]{5}\\b" // notion code
        );
        List<String> codes = new ArrayList<>();
        for(String pattern : patterns){
            Pattern regexPattern = Pattern.compile(pattern);
            Matcher matcher = regexPattern.matcher(text);
            while(matcher.find()){
                codes.add(matcher.group());
            }
        }
        codes = codes.stream().distinct().toList();
        return codes;
    }

    private List<String> extractVerificationLink(Element element){
        List<String> links = new ArrayList<>();
        Elements anchorElements = element.getElementsByTag("a");
        for(Element anchorElement : anchorElements){
            String href = anchorElement.attr("href");
            if(href.startsWith("https")) links.add(href);
        }
        return links;
    }

    private boolean isVerificationEmail(String decodedContent){
        Document doc = Jsoup.parse(decodedContent);
        String bodyText = doc.body().text().toLowerCase();
        List<String> englishKeywords = readKeywords("src/main/resources/keywords_en.txt");
        List<String> koreanKeywords = readKeywords("src/main/resources/keywords_ko.txt");
        for(String englishKeyword : englishKeywords){
            if(bodyText.contains(englishKeyword)){
                return true;
            }
        }
        for(String koreanKeyword : koreanKeywords){
            if(bodyText.contains(koreanKeyword)){
                return true;
            }
        }
        return false;
    }

    private List<String> readKeywords(String fileName){
        List<String> keywords = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] newKeywords = line.split(",");
                keywords.addAll(Arrays.stream(newKeywords).map(String::trim).toList());
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return keywords;
    }
}
