package woozlabs.echo.domain.member.repository.query;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import woozlabs.echo.domain.member.entity.MemberAccount;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MemberAccountQueryRepository {
    private final EntityManager em;
    public Optional<MemberAccount> findByMemberUidAndAccountUid(String uid, String accountUid){
        String jpql = "select ma from MemberAccount ma" +
                " join fetch ma.account" +
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

    public List<MemberAccount> findByMemberPrimaryUid(String uid){
        String jpql = "select ma from MemberAccount ma" +
                " fetch join ma.account" +
                " where ma.member.primaryUid = :uid";
        return em.createQuery(jpql, MemberAccount.class)
                .setParameter("uid", uid)
                .getResultList();
    }
}
