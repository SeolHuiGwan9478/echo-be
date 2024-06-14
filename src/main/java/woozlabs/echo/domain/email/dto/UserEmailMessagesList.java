package woozlabs.echo.domain.email.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserEmailMessagesList {
    private List<UserEmailMessagesListData> messages;
    private String nextPageToken;
    private int resultSizeEstimate;
}
