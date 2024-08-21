package woozlabs.echo.domain.gmail.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import woozlabs.echo.domain.calendar.dto.UnAvailableDatesResponse;
import woozlabs.echo.domain.calendar.service.CalendarService;
import woozlabs.echo.domain.chatGPT.service.ChatGptService;
import woozlabs.echo.domain.gmail.dto.extract.ExtractScheduleInfo;
import woozlabs.echo.domain.gmail.dto.extract.ExtractVerificationInfo;
import woozlabs.echo.domain.gmail.dto.extract.GenScheduleEmailTemplateResponse;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class GmailUtility {
    private final String DOMAIN_PATTERN = "(?i)^(https?://(?:www\\.)?[^/]+)";
    private final String ID_PATTERN = "id=(\\d+)";

    private final ObjectMapper om;
    private final ChatGptService chatGptService;
    private final CalendarService calendarService;
    private List<String> keywords;

    @PostConstruct
    public void initKeywords(){
        this.keywords = readKeywords("keywords_en.txt");
        this.keywords.addAll(readKeywords("keywords_ko.txt"));
    }

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

    public ExtractScheduleInfo extractSchedule(String decodedContent) throws JsonProcessingException {
        String result = chatGptService.analyzeScheduleEmail(decodedContent);
        return om.readValue(result, ExtractScheduleInfo.class);
    }

    public GenScheduleEmailTemplateResponse generateScheduleEmailTemplate(String uid, String decodedContent) throws IOException, GeneralSecurityException {
        UnAvailableDatesResponse unAvailableDatesResponse = calendarService.getDatesWithNoEventsInTwoWeeks(uid);
        List<String> unAvailableDates = unAvailableDatesResponse.getUnavailableDates();
        String result = chatGptService.generateScheduleEmailTemplate(decodedContent, unAvailableDates);
        return om.readValue(result, GenScheduleEmailTemplateResponse.class);
    }

    private List<String> getVerificationLink(String decodedContent){
        Document doc = Jsoup.parse(decodedContent, "UTF-8");
        List<String> links = new ArrayList<>();
        List<String> contents = new ArrayList<>();
        for(String keyword : keywords){
            List<Element> elements = new ArrayList<>();
            for(Element element : doc.getAllElements()){
                if(((element.is("a") && element.text().toLowerCase().contains(keyword)) || element.ownText().toLowerCase().contains(keyword))
                        && !contents.contains(element.text())){
                    elements.add(element);
                    contents.add(element.text());
                }
            }
            if(elements.isEmpty()) continue;
            Elements convertElements = new Elements(elements);;
            links.addAll(extractVerificationLink(convertElements));
        }
        return links.stream().distinct().toList();
    }

    private List<String> getVerificationCode(String decodedContent){
        Document doc = Jsoup.parse(decodedContent, "UTF-8");
        doc.select("tbdoy tr, td, th, thead, tfoot").unwrap(); // unwrap
        List<String> codes = new ArrayList<>();
        List<String> contents = new ArrayList<>();
        for(String keyword : keywords){
            for(Element element : doc.getAllElements()){
                if(element.ownText().toLowerCase().contains(keyword)
                        && !contents.contains(element.text())){
                    contents.add(element.text());
                    codes.addAll(extractVerificationCode(element.text()));
                }
            }
        }
        return codes.stream().distinct().toList();
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
            links.addAll(extractCoreContentLink(elements));
        }catch (Exception e){
            throw new CustomErrorException(ErrorCode.EXTRACT_VERIFICATION_LINK_ERR, e.getMessage());
        }
        return links;
    }

    private boolean isVerificationEmail(String decodedContent){
        Document doc = Jsoup.parse(decodedContent, "UTF-8");
        String bodyText = doc.body().text().toLowerCase();
        for(String keyword : keywords){
            if(bodyText.contains(keyword)){
                return true;
            }
        }
        return false;
    }

    private List<String> readKeywords(String fileName){
        List<String> keywords = new ArrayList<>();
        Resource resource = new ClassPathResource(fileName);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
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

    private List<String> extractCoreContentCode(Elements coreElements){
        int attrId = 1;
        List<Element> elementsToRemove = new ArrayList<>();
        List<String> verificationInfo = new ArrayList<>();
        for (Element coreElement : coreElements){
            if(coreElement.text().isEmpty()){ // empty tag
                elementsToRemove.add(coreElement);
            }else if(coreElement.is("style, script, head, title, meta, img, br")){ // necessary removal tags
                elementsToRemove.add(coreElement);
            }else{
                coreElement.clearAttributes();
            }
        }
        for(Element element : elementsToRemove){
            coreElements.remove(element);
        }

        for(Element coreElement : coreElements){
            // numbering 4-digits
            coreElement.attr("id", String.format("%04d", attrId));
            attrId += 1;
        }

        // running gpt
        String resultGpt = chatGptService.analyzeVerificationEmail(coreElements.toString());
        if(resultGpt.equals("false")){
            return verificationInfo;
        }else{
            Pattern pattern = Pattern.compile(ID_PATTERN);
            Matcher matcher = pattern.matcher(resultGpt);
            if(matcher.find()){
                String idValue = matcher.group(1);
                String cssSelector = "#" + idValue;
                Elements searchElements = coreElements.select(cssSelector);
                for(Element element : searchElements){
                    if(element.attr("id").equals(idValue)){
                        verificationInfo.add(element.text());
                    }
                }
            }
            return verificationInfo;
        }
    }

    private List<String> extractCoreContentLink(Elements coreElements) {
        int attrId = 1;
        List<Element> elementsToRemove = new ArrayList<>();
        List<String> verificationInfo = new ArrayList<>();
        for (Element coreElement : coreElements){
            if(coreElement.text().isEmpty() && !coreElement.is("a")){ // empty tag
                elementsToRemove.add(coreElement);
            }else if(coreElement.is("style, script, head, title, meta, img, br")){ // necessary removal tags
                elementsToRemove.add(coreElement);
            }else{
                if(coreElement.hasAttr("href")){
                    String url = coreElement.attr("href");
                    coreElement.clearAttributes();
                    coreElement.attr("href", url);
                }else{
                    coreElement.clearAttributes();
                }
            }
        }
        for(Element element : elementsToRemove){
            coreElements.remove(element);
        }

        Elements beforeOptimizeElements = coreElements.clone();
        // optimization url & numbering
        for(int idx = 0;idx < coreElements.size();idx++){
            // init
            Element coreElement = coreElements.get(idx);
            Element beforeOptimizeElement = beforeOptimizeElements.get(idx);
            // optimization
            if(coreElement.is("a") && coreElement.hasAttr("href")){
                String originUrl = coreElement.attr("href");
                Pattern pattern = Pattern.compile(DOMAIN_PATTERN);
                Matcher matcher = pattern.matcher(originUrl);
                if(matcher.find()) { // optimization href link
                    coreElement.attr("href", matcher.group(1));
                }
                Matcher textMatcher = pattern.matcher(coreElement.text());
                if(textMatcher.find()){ // optimization text link
                    coreElement.text(textMatcher.group(1));
                }
            }
            // numbering 4-digits
            coreElement.attr("id", String.format("%04d", attrId));
            beforeOptimizeElement.attr("id", String.format("%04d", attrId));
            attrId += 1;
        };

        // running gpt
        String resultGpt = chatGptService.analyzeVerificationEmail(coreElements.toString());
        if(resultGpt.equals("false")){
            return verificationInfo;
        }else{
            Pattern pattern = Pattern.compile(ID_PATTERN);
            Matcher matcher = pattern.matcher(resultGpt);
            if(matcher.find()){
                String idValue = matcher.group(1);
                String cssSelector = "a[id='" + idValue + "']";
                Elements searchElements = beforeOptimizeElements.select(cssSelector);
                for(Element element : searchElements){
                    if(element.attr("id").equals(idValue) && element.hasAttr("href")){
                        verificationInfo.add(element.attr("href"));
                    }
                }
            }
            return verificationInfo;
        }
    }
}
