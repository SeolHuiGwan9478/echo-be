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
        // local var
        StringBuilder q = new StringBuilder();
        final String FROM_QUERY_KEY = "from:";
        final String TO_QUERY_KEY = "to:";
        final String SUBJECT_QUERY_KEY = "subject:";
        // generate query
        List<String> queries = new ArrayList<>();
        if(from != null) queries.add(FROM_QUERY_KEY + from);
        if(to != null) queries.add(TO_QUERY_KEY + to);
        if(subject != null) queries.add(SUBJECT_QUERY_KEY + subject);
        if(query != null) queries.add(query);
        for(int idx = 0;idx < queries.size();idx++){
            q.append(queries.get(idx));
            if(idx != queries.size()-1) q.append(" OR ");
        }
        return q.toString();
    }
}