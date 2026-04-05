package com.medpoint.enums;

/**
 * Tags a Transaction to its originating operational module.
 */
public enum TxModule {
    DRUGSTORE,
    MART,
    HOTEL,
    RESTAURANT,
    STORE;

    public String toKey() { return name().toLowerCase(); }
}
