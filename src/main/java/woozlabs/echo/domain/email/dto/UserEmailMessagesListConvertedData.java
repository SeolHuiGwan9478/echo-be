package woozlabs.echo.domain.email.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserEmailMessagesListConvertedData {
    private String id;
    private List<String> labelIds;
    private String snippet;
}