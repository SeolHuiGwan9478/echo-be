package woozlabs.echo.domain.contact.controller;

import com.google.api.services.people.v1.model.Person;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import woozlabs.echo.domain.contact.service.GooglePeopleService;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/echo/contacts")
public class GooglePeopleController {

    private final GooglePeopleService googlePeopleService;

    @GetMapping("/other")
    public List<Person> getOtherContacts() throws IOException {
        return googlePeopleService.getOtherContacts();
    }
}
