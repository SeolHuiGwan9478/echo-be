package woozlabs.echo.domain.member.repository.query;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import woozlabs.echo.domain.member.entity.MemberAccount;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MemberAccountQueryRepository {
    private final EntityManager em;
    public Optional<MemberAccount> findByMemberUidAndAccountUid(String uid, String accountUid){
        String jpql = "select ma from MemberAccount ma" +
                " where ma.member.primaryUid = :uid" +
                " and ma.account.uid = :accountUid";
        try{
            MemberAccount memberAccount =  em.createQuery(jpql, MemberAccount.class)
                    .setParameter("uid", uid)
                    .setParameter("accountUid", accountUid)
                    .getSingleResult();
            return Optional.of(memberAccount);
        }catch (Exception e){
            return Optional.empty();
        }
    }
}
