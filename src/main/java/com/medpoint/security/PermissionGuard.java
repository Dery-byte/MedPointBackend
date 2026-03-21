package com.medpoint.security;
import com.medpoint.entity.User;
import com.medpoint.enums.ManageModule;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Spring Security @Bean used in @PreAuthorize SpEL expressions.
 * Example: @PreAuthorize("@permissionGuard.canManageDrugstore(authentication)")
 *
 * This separates module-management permission checks from role checks,
 * keeping them aligned with ManageModule enum values rather than hard-coded strings.
 */
@Component("permissionGuard")
public class PermissionGuard {

    public boolean canManageDrugstore(Authentication auth) {
        return canManage(auth, ManageModule.DRUGSTORE);
    }

    public boolean canManageMart(Authentication auth) {
        return canManage(auth, ManageModule.MART);
    }

    public boolean canManageHotel(Authentication auth) {
        return canManage(auth, ManageModule.HOTEL);
    }

    public boolean canManageRestaurant(Authentication auth) {
        return canManage(auth, ManageModule.RESTAURANT);
    }

    private boolean canManage(Authentication auth, ManageModule module) {
        if (auth == null || !auth.isAuthenticated()) return false;
        Object principal = auth.getPrincipal();
        if (!(principal instanceof User user)) return false;
        return user.canManage(module);
    }
}
