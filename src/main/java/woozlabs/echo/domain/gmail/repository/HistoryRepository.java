package woozlabs.echo.domain.gmail.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import woozlabs.echo.domain.gmail.entity.History;

public interface HistoryRepository extends JpaRepository<History, Long> {
}
