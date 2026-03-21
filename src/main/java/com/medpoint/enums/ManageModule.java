package com.medpoint.enums;

/**
 * Modules a staff member can ADMINISTER:
 * add / edit / price / delete catalogue items within that module.
 * SUPERADMIN implicitly holds all manage permissions.
 */
public enum ManageModule {
    DRUGSTORE,
    MART,
    HOTEL,
    RESTAURANT;

    public String toKey() { return name().toLowerCase(); }
}
