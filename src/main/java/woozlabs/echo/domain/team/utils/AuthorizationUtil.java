package woozlabs.echo.domain.team.utils;

import woozlabs.echo.domain.team.entity.Role;
import woozlabs.echo.domain.sharedEmail.entity.ShareStatus;
import woozlabs.echo.domain.team.entity.TeamAccount;

public class AuthorizationUtil {

    public static boolean canViewSharedEmail(TeamAccount teamAccount, ShareStatus shareStatus) {
        if (teamAccount == null) {
            return shareStatus == ShareStatus.PUBLIC;
        }

        Role role = teamAccount.getRole();
        return role == Role.ADMIN || role == Role.EDITOR ||
                (role == Role.VIEWER && (shareStatus == ShareStatus.PUBLIC || shareStatus == ShareStatus.TEAM)) ||
                (role == Role.PUBLIC_VIEWER && shareStatus == ShareStatus.PUBLIC);
    }

    public static boolean canSharedEmail(TeamAccount teamAccount) {
        return teamAccount != null && (teamAccount.getRole() == Role.ADMIN);
    }

    public static boolean canEditSharedEmail(TeamAccount teamAccount) {
        return teamAccount != null && (teamAccount.getRole() == Role.ADMIN || teamAccount.getRole() == Role.EDITOR);
    }

    public static boolean canDeleteSharedEmail(TeamAccount teamAccount) {
        return teamAccount != null && (teamAccount.getRole() == Role.ADMIN);
    }
}
