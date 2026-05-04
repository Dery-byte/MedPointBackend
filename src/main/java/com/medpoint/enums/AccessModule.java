package com.medpoint.enums;

/**
 * Modules a staff member can ACCESS (operate the POS / check-in desk / etc.).
 * Assigned per user by an admin. SUPERADMIN implicitly holds all.
 */
public enum AccessModule {
    DRUGSTORE,
    MART,
    HOTEL,
    STOREFRONT,
    RESTAURANT;
    public String toKey() { return name().toLowerCase(); }
}
