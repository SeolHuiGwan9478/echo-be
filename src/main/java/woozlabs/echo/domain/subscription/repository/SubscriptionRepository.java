package woozlabs.echo.domain.subscription.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import woozlabs.echo.domain.subscription.entity.Subscription;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
}
