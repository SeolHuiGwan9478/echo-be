package woozlabs.echo.domain.gemini.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProofreadResponse {

    private String correctedText;
    private List<Change> changes;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Change {

        private String original;
        private String modified;
        private String reason;
    }
}
