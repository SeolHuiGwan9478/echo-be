package woozlabs.echo.domain.gemini.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class GeminiServiceTest {

    @Autowired
    private GeminiService geminiService;

    @Test
    public void getCompetion() throws Exception {
        String text = geminiService.getCompletion("서울 맛집을 추천해줘");
        System.out.println(text);
    }

}