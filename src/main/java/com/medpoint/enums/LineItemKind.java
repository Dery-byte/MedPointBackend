package com.medpoint.enums;

/**
 * Whether a transaction line-item is a physical product / drug (ITEM)
 * or a rendered clinical service (SERVICE).
 */
public enum LineItemKind {
    ITEM,
    SERVICE,
    DRUG,
}
