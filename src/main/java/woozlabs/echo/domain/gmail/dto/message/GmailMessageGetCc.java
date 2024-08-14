package woozlabs.echo.domain.gmail.dto.message;

import lombok.Data;
import woozlabs.echo.domain.gmail.dto.thread.GmailThreadGetMessagesCc;

import java.util.Objects;

@Data
public class GmailMessageGetCc {
    private String name;
    private String email;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GmailMessageGetCc that = (GmailMessageGetCc) o;
        return this.name.equals(that.getName()) &&
                this.email.equals(that.getEmail());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, email);
    }
}
