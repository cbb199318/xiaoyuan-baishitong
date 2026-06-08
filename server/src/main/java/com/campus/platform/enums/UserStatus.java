package com.campus.platform.enums;

public enum UserStatus {
    ACTIVE,
    WARNED,
    ERRAND_RESTRICTED,
    TEMP_BANNED,
    PERMANENT_BANNED,
    BLACKLISTED,
    DISABLED;

    public static boolean canLogin(String status) {
        if (status == null || status.isBlank()) {
            return false;
        }
        return ACTIVE.name().equalsIgnoreCase(status)
            || WARNED.name().equalsIgnoreCase(status)
            || ERRAND_RESTRICTED.name().equalsIgnoreCase(status);
    }

    public static boolean canUseErrand(String status) {
        if (status == null || status.isBlank()) {
            return false;
        }
        return ACTIVE.name().equalsIgnoreCase(status)
            || WARNED.name().equalsIgnoreCase(status);
    }
}
