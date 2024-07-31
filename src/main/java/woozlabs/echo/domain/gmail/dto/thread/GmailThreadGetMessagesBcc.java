package woozlabs.echo.domain.gmail.dto.thread;

import lombok.Data;

import java.util.Objects;

@Data
public class GmailThreadGetMessagesBcc {
    private String name;
    private String email;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GmailThreadGetMessagesBcc that = (GmailThreadGetMessagesBcc) o;
        return this.name.equals(that.getName()) &&
                this.email.equals(that.getEmail());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, email);
    }
}