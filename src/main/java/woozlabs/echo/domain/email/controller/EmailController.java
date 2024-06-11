package woozlabs.echo.domain.email.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import woozlabs.echo.global.dto.ResponseDto;

@RestController
public class EmailController {
    @GetMapping("/api/v1/email/messages")
    public ResponseEntity<ResponseDto> getEmailMessages(){
        return new ResponseEntity<>(HttpStatus.OK);
    }
}