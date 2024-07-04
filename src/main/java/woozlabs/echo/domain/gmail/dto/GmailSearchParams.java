package woozlabs.echo.domain.gmail.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class GmailSearchParams {
    private String from;
    private String to;
    private String subject;
    private String query;

    public String createQ(){
        StringBuilder q = new StringBuilder();
        List<String> queries = new ArrayList<>();
        if(from != null) queries.add("from:" + from);
        if(to != null) queries.add("to:" + to);
        if(subject != null) queries.add("subject:" + subject);
        queries.add(query);
        for(int idx = 0;idx < queries.size();idx++){
            q.append(query);
            if(idx != queries.size()-1) q.append(" OR ");
        }
        return q.toString();
    }
}