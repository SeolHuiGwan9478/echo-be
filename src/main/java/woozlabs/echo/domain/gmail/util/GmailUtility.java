package woozlabs.echo.domain.gmail.util;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import woozlabs.echo.domain.chatGPT.service.ChatGptService;
import woozlabs.echo.domain.gmail.dto.thread.GmailThreadGetBody;
import woozlabs.echo.domain.gmail.dto.thread.GmailThreadGetPart;
import woozlabs.echo.domain.gmail.dto.thread.GmailThreadGetPayload;
import woozlabs.echo.domain.gmail.dto.verification.ExtractVerificationInfo;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class GmailUtility {
    private final String DOMAIN_PATTERN = "(?i)^(https?://(?:www\\.)?[^/]+)";
    private final String ID_PATTERN = "id=(\\d+)";
    private final ChatGptService chatGptService;

    public ExtractVerificationInfo extractVerification(String rawContent){
        List<String> codes = new ArrayList<>();
        List<String> links = new ArrayList<>();
        ExtractVerificationInfo extractVerificationInfo = new ExtractVerificationInfo();
        if(rawContent == null){
            return extractVerificationInfo;
        }
        // Convert URL-safe Base64 to standard Base64
        String standardBase64 = rawContent
                .replace('-', '+')
                .replace('_', '/');
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
        Document doc = Jsoup.parse(decodedContent, "UTF-8");
        List<String> links = new ArrayList<>();
        List<String> keywords = readKeywords("src/main/resources/keywords_en.txt");
        keywords.addAll(readKeywords("src/main/resources/keywords_ko.txt"));
        for(String keyword : keywords){
            List<Element> elements = doc.getAllElements().stream().filter((element) -> element.ownText().toLowerCase().contains(keyword)).toList();
            if(elements.isEmpty()) continue;
            Elements convertElements = new Elements(elements);
            links.addAll(extractVerificationLink(convertElements));
            break;
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

    private List<String> extractVerificationLink(Elements elements){
        List<String> links = new ArrayList<>();
        try {
            links.addAll(extractCoreContent(elements.toString()));
        }catch (Exception e){
            System.out.println(elements.toString());
            System.out.println(e.getMessage());
            System.out.println("here!!");
        }
        return links;
    }

    private boolean isVerificationEmail(String decodedContent){
        Document doc = Jsoup.parse(decodedContent, "UTF-8");
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
            throw new CustomErrorException(ErrorCode.KEYWORD_IO_EXCEPTION, e.getMessage());
        }
        return keywords;
    }

    private List<String> extractCoreContent(String htmlContent) {
        int attrId = 1;
        List<Element> elementsToRemove = new ArrayList<>();
        List<String> verificationInfo = new ArrayList<>();
        Document doc = Jsoup.parse(htmlContent, "UTF-8");
        doc.select("style, script, head, title, meta, img").remove();
        Elements coreElements = doc.getAllElements();
        // remove &nbsp tag
        for (Element corElement : coreElements){
            if(corElement.html().contains("&nbsp")){
                elementsToRemove.add(corElement);
            }else if(corElement.text().isEmpty() && !corElement.is("a")){
                elementsToRemove.add(corElement);
            }else if(corElement.hasAttr("class") && corElement.attr("class").equals("gmail_attr")){
                elementsToRemove.add(corElement);
            }
        }
        for(Element element : elementsToRemove){
            coreElements.remove(element);
        }
        // remove attributes
        for(Element coreElement : coreElements){
            Attributes attributes = coreElement.attributes();
            attributes.forEach((attr) -> {
                if(!attr.getKey().equals("href")){
                    coreElement.removeAttr(attr.getKey());
                }
            });
        }

        Elements beforeOptimizeElements = coreElements.clone();
        // optimization url
        for(Element coreElement : coreElements){
            if(coreElement.is("a") && coreElement.hasAttr("href")){
                String originUrl = coreElement.attr("href");
                Pattern pattern = Pattern.compile(DOMAIN_PATTERN);
                Matcher matcher = pattern.matcher(originUrl);
                if(matcher.find()){
                    coreElement.attr("href", matcher.group(1));
                }else{
                    coreElement.attr("href", "https://www.echoisbest.com");
                }
            }
        };
        // numbering 4-digits
        for (Element coreElement : coreElements) {
            coreElement.attr("id", String.format("%04d", attrId));
            beforeOptimizeElements.attr("id", String.format("%04d", attrId));
            attrId += 1;
        }

        System.out.println("----------start---------");
        System.out.println(coreElements.toString().length());
        System.out.println(coreElements.toString());
        System.out.println("----------end----------");
        // running gpt
        String resultGpt = chatGptService.analyzeVerificationEmail(coreElements.toString());
        if(resultGpt.equals("false")){
            return verificationInfo;
        }else{
            Pattern pattern = Pattern.compile(ID_PATTERN);
            Matcher matcher = pattern.matcher(resultGpt);
            if(matcher.find()){
                String idValue = matcher.group(1);
                for(Element element : beforeOptimizeElements){
                    if(element.attr("id").equals(idValue) && element.hasAttr("href")){
                        verificationInfo.add(element.attr("href"));
                    }else{
                        verificationInfo.add(element.text());
                    }
                }
            }
            return verificationInfo;
        }
    }
}
