package woozlabs.echo.domain.team.utils;

import woozlabs.echo.domain.team.entity.TeamMemberRole;
import woozlabs.echo.domain.sharedEmail.entity.ShareStatus;
import woozlabs.echo.domain.team.entity.TeamMember;

public class AuthorizationUtil {

    public static boolean canViewSharedEmail(TeamMember teamMember, ShareStatus shareStatus) {
        if (teamMember == null) {
            return shareStatus == ShareStatus.PUBLIC;
        }

        TeamMemberRole teamMemberRole = teamMember.getRole();
        return teamMemberRole == TeamMemberRole.ADMIN || teamMemberRole == TeamMemberRole.EDITOR ||
                (teamMemberRole == TeamMemberRole.VIEWER && (shareStatus == ShareStatus.PUBLIC || shareStatus == ShareStatus.TEAM)) ||
                (teamMemberRole == TeamMemberRole.PUBLIC_VIEWER && shareStatus == ShareStatus.PUBLIC);
    }

    public static boolean canSharedEmail(TeamMember teamMember) {
        return teamMember != null && (teamMember.getRole() == TeamMemberRole.ADMIN);
    }

    public static boolean canEditSharedEmail(TeamMember teamMember) {
        return teamMember != null && (teamMember.getRole() == TeamMemberRole.ADMIN || teamMember.getRole() == TeamMemberRole.EDITOR);
    }

    public static boolean canDeleteSharedEmail(TeamMember teamMember) {
        return teamMember != null && (teamMember.getRole() == TeamMemberRole.ADMIN);
    }
}
