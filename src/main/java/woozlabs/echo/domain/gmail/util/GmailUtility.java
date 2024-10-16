package woozlabs.echo.domain.gmail.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import woozlabs.echo.domain.chatGPT.service.ChatGptService;
import woozlabs.echo.domain.gmail.dto.template.ExtractScheduleInfo;
import woozlabs.echo.domain.gmail.dto.template.ExtractVerificationInfo;
import woozlabs.echo.domain.member.entity.Account;
import woozlabs.echo.domain.member.entity.MemberAccount;
import woozlabs.echo.domain.member.repository.query.MemberAccountQueryRepository;
import woozlabs.echo.global.constant.GlobalConstant;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class GmailUtility {
    private final String DOMAIN_PATTERN = "(?i)^(https?://(?:www\\.)?[^/]+)";
    private final String ID_PATTERN = "id=(\\d+)";
    private final String ELEMENT_PATTERN = "<element=(.*?),";

    private final ObjectMapper om;
    private final ChatGptService chatGptService;
    private final MemberAccountQueryRepository memberAccountQueryRepository;
    private final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private List<String> keywords;
    private final List<String> SCOPES = Arrays.asList(
            "https://www.googleapis.com/auth/gmail.readonly",
            "https://www.googleapis.com/auth/userinfo.profile",
            "https://www.googleapis.com/auth/userinfo.email",
            "https://www.googleapis.com/auth/gmail.modify",
            "https://mail.google.com/",
            "https://www.googleapis.com/auth/gmail.settings.sharing",
            "https://www.googleapis.com/auth/gmail.settings.basic"
    );

    @PostConstruct
    public void initKeywords(){
        this.keywords = readKeywords("keywords_en.txt");
        this.keywords.addAll(readKeywords("keywords_ko.txt"));
    }

    public String getActiveAccountAccessToken(HttpServletRequest request, String aAUid){
        String uid = (String) request.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
        MemberAccount memberAccount = memberAccountQueryRepository.findByMemberUidAndAccountUid(uid, aAUid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER_ACCOUNT));
        Account account = memberAccount.getAccount();
        return account.getAccessToken();
    }

    public Account getActiveAccount(HttpServletRequest request, String aAUid){
        String uid = (String) request.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
        MemberAccount memberAccount = memberAccountQueryRepository.findByMemberUidAndAccountUid(uid, aAUid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER_ACCOUNT));
        return memberAccount.getAccount();
    }

    public String getActiveAccountUid(HttpServletRequest request, String aAUid){
        String uid = (String) request.getAttribute(GlobalConstant.FIREBASE_UID_KEY);
        MemberAccount memberAccount = memberAccountQueryRepository.findByMemberUidAndAccountUid(uid, aAUid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_MEMBER_ACCOUNT));
        return memberAccount.getAccount().getUid();
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
        List<String> link_test = getVerificationLink(decodedContent);
        links.addAll(link_test);
        if(links.isEmpty()){
            codes.addAll(getVerificationCode(decodedContent));
        }
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

    public Gmail createGmailService(String accessToken) {
        try{
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            HttpRequestInitializer requestInitializer = createCredentialWithAccessToken(accessToken);
            return new Gmail.Builder(httpTransport, JSON_FACTORY, requestInitializer)
                    .setApplicationName("Echo")
                    .build();
        }catch (Exception e){
            throw new CustomErrorException(ErrorCode.FAILED_TO_GET_GMAIL_CONNECTION_REQUEST, e.getMessage());
        }
    }

    public File convertMultipartFileToTempFile(MultipartFile multipartFile) throws IOException {
        File tempFile = File.createTempFile("",multipartFile.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(multipartFile.getBytes());
        }
        tempFile.deleteOnExit();
        return tempFile;
    }

    private HttpRequestInitializer createCredentialWithAccessToken(String accessToken) {
        AccessToken token = AccessToken.newBuilder()
                .setTokenValue(accessToken)
                .setScopes(SCOPES)
                .build();

        GoogleCredentials googleCredentials = GoogleCredentials.create(token);

        return httpRequest -> {
            new HttpCredentialsAdapter(googleCredentials).initialize(httpRequest);
            // setting timeout
            httpRequest.setConnectTimeout(3 * 60 * 1000);  // connect timeout 3분
            httpRequest.setReadTimeout(5 * 60 * 1000);     // read timeout 5분
        };
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
        doc.select("tbody, tr, thead, tfoot, table").unwrap(); // unwrap
        List<String> regexCodes = new ArrayList<>();
        List<String> codes = new ArrayList<>();
        List<String> contents = new ArrayList<>();
        for(String keyword : keywords){
            List<Element> elements = new ArrayList<>();
            for(Element element : doc.getAllElements()) {
                if (element.text().toLowerCase().contains(keyword)
                        && !contents.contains(element.text())) {
                    elements.add(element);
                    regexCodes.addAll(extractVerificationCode(element.text()));
                    contents.add(element.text());
                }
            }
            if(elements.isEmpty()) continue;
            Elements convertElements = new Elements(elements);
            codes.addAll(extractCoreContentCode(convertElements));
        }
        if(!regexCodes.isEmpty()){
            return regexCodes.stream().distinct().toList();
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
            coreElement.getAllElements().forEach((element) -> {
                if(element.text().isEmpty()){ // empty tag
                    element.remove();
                }else if(element.is("style, script, head, title, meta, img, br")){ // necessary removal tags
                    element.remove();
                }else{
                    element.clearAttributes();
                }
            });
        }
        for(Element coreElement : coreElements){
            // numbering 4-digits
            for(Element element : coreElement.getAllElements()) {
                element.attr("id", String.format("%04d", attrId));
                attrId += 1;
            }
        }

        // running gpt
        String resultGpt = chatGptService.analyzeVerificationEmail(coreElements.toString());
        if(resultGpt.equals("false") || resultGpt.equals("unknown")){
            return verificationInfo;
        }else{
            Pattern idPattern = Pattern.compile(ID_PATTERN);
            Pattern elementPattern = Pattern.compile(ELEMENT_PATTERN);
            Matcher idMatcher = idPattern.matcher(resultGpt);
            Matcher elementMatcher = elementPattern.matcher(resultGpt);
            if(idMatcher.find() && elementMatcher.find()){
                String elementValue = elementMatcher.group(1);
                if(!elementValue.strip().equals("CODE")){
                    return verificationInfo;
                }
                String idValue = idMatcher.group(1);
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
            } else{
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
        if(resultGpt.equals("false") || resultGpt.equals("unknown")){
            return verificationInfo;
        }else{
            Pattern idPattern = Pattern.compile(ID_PATTERN);
            //Pattern elementPattern = Pattern.compile(ELEMENT_PATTERN);
            Matcher idMatcher = idPattern.matcher(resultGpt);
            //Matcher elementMatcher = elementPattern.matcher(resultGpt);
            if(idMatcher.find()){
                //String elementValue = elementMatcher.group(1);
                String idValue = idMatcher.group(1);
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
