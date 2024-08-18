package woozlabs.echo.domain.team.utils;

import woozlabs.echo.domain.team.entity.Role;
import woozlabs.echo.domain.team.entity.ShareStatus;
import woozlabs.echo.domain.team.entity.TeamMember;

public class AuthorizationUtil {

    public static boolean canViewSharedEmail(TeamMember teamMember, ShareStatus shareStatus) {
        if (teamMember == null) {
            return shareStatus == ShareStatus.PUBLIC;
        }

        Role role = teamMember.getRole();
        return role == Role.ADMIN || role == Role.EDITOR ||
                (role == Role.VIEWER && (shareStatus == ShareStatus.PUBLIC || shareStatus == ShareStatus.TEAM)) ||
                (role == Role.PUBLIC_VIEWER && shareStatus == ShareStatus.PUBLIC);
    }

    public static boolean canEditSharedEmail(TeamMember teamMember) {
        return teamMember != null && (teamMember.getRole() == Role.ADMIN || teamMember.getRole() == Role.EDITOR);
    }

    public static boolean canDeleteSharedEmail(TeamMember teamMember) {
        return teamMember != null && (teamMember.getRole() == Role.ADMIN || teamMember.getRole() == Role.EDITOR);
    }
}
