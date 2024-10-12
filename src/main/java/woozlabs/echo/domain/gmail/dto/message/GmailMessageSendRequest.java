package woozlabs.echo.domain.gmail.dto.message;

import lombok.Data;

import java.io.File;
import java.util.List;

@Data
public class GmailMessageSendRequest {
    private String toEmailAddress;
    private String fromEmailAddress;
    private String subject;
    private String bodyText;
    private List<File> files;
}