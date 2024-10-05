package woozlabs.echo.domain.gmail.dto.message;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class GmailMessageSendRequest {
    private String toEmailAddress;
    private String fromEmailAddress;
    private String subject;
    private String bodyText;
    private List<MultipartFile> files;
    private List<MultipartFile> inlines;
}