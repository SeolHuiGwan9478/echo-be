package woozlabs.echo.domain.team.utils;

import woozlabs.echo.domain.sharedEmail.entity.Access;
import woozlabs.echo.domain.sharedEmail.entity.Permission;
import woozlabs.echo.domain.sharedEmail.entity.SharedEmail;
import woozlabs.echo.domain.team.entity.Role;
import woozlabs.echo.domain.team.entity.TeamAccount;

public class AuthorizationUtil {

//    public static boolean canViewSharedEmail(TeamAccount teamAccount, SharedEmail sharedEmail) {
//        if (teamAccount == null) {
//            return sharedEmail.getAccess() == Access.PUBLIC;
//        }
//
//        Permission permission = getPermissionForTeamAccount(teamAccount, sharedEmail);
//        return permission == Permission.ADMIN || permission == Permission.EDITOR ||
//                (permission == Permission.VIEWER && (sharedEmail.getAccess() == Access.PUBLIC || sharedEmail.getAccess() == Access.TEAM)) ||
//                (permission == Permission.PUBLIC_VIEWER && sharedEmail.getAccess() == Access.PUBLIC);
//    }
//
//    public static boolean canSharedEmail(TeamAccount teamAccount) {
//        return teamAccount != null && (teamAccount.getRole() == Role.ADMIN);
//    }
//
//    public static boolean canEditSharedEmail(TeamAccount teamAccount) {
//        return teamAccount != null && (teamAccount.getRole() == Role.ADMIN || teamAccount.getRole() == Role.EDITOR);
//    }
//
//    public static boolean canDeleteSharedEmail(TeamAccount teamAccount) {
//        return teamAccount != null && (teamAccount.getRole() == Role.ADMIN);
//    }
}
