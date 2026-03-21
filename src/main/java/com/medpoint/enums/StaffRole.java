package com.medpoint.enums;

/**
 * Role hierarchy for MedPoint staff.
 *
 * SUPERADMIN – unrestricted; full admin panel; all modules automatically.
 * MANAGER    – assigned modules + optional manage permissions granted by admin.
 * STAFF      – assigned modules only; no admin panel access.
 */
public enum StaffRole {
    SUPERADMIN,
    MANAGER,
    STAFF;

    public String toKey() { return name().toLowerCase(); }
}
