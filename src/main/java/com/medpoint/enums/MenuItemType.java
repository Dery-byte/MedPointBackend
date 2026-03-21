package com.medpoint.enums;

public enum MenuItemType {
    FOOD,
    DRINK;

    public String toKey() { return name().toLowerCase(); }
}
