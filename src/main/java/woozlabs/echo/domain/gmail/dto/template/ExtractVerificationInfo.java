package woozlabs.echo.domain.gmail.dto.template;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class ExtractVerificationInfo {
    private Boolean verification = Boolean.FALSE;
    private List<String> codes = new ArrayList<>();
    private List<String> links = new ArrayList<>();

    public void updateCodes(List<String> newCodes){
        Set<String> uniqueCodes = new HashSet<>(this.codes);
        uniqueCodes.addAll(newCodes);
        this.codes = new ArrayList<>(uniqueCodes);
    }

    public void updateLinks(List<String> newLinks){
        Set<String> uniqueLinks = new HashSet<>(this.links);
        uniqueLinks.addAll(newLinks);
        this.links = new ArrayList<>(uniqueLinks);
    }
}
