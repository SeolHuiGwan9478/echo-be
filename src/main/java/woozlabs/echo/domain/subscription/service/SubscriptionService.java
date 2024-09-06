package woozlabs.echo.domain.subscription.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import woozlabs.echo.domain.member.entity.Account;
import woozlabs.echo.domain.member.repository.AccountRepository;
import woozlabs.echo.domain.subscription.entity.Plan;
import woozlabs.echo.domain.subscription.entity.Subscription;
import woozlabs.echo.domain.subscription.repository.SubscriptionRepository;
import woozlabs.echo.global.exception.CustomErrorException;
import woozlabs.echo.global.exception.ErrorCode;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final AccountRepository accountRepository;
    private final PaymentService paymentService;

    @Transactional
    public void activateSubscription(String uid) {
        Account account = accountRepository.findByUid(uid)
                .orElseThrow(() -> new CustomErrorException(ErrorCode.NOT_FOUND_ACCOUNT_ERROR_MESSAGE));

        paymentService.processPayment(account.getMember());

        Subscription subscription = Subscription.builder()
                .member(account.getMember())
                .plan(Plan.PLUS)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusMonths(1))
                .build();

        subscriptionRepository.save(subscription);
    }

}
