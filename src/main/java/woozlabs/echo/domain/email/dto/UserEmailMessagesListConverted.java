package woozlabs.echo.domain.email.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserEmailMessagesListConverted {
    List<UserEmailMessagesListConvertedData> messages;
}