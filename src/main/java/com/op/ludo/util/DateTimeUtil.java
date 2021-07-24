package com.op.ludo.util;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class DateTimeUtil {

    private DateTimeUtil() {}

    public static final String SYSTEM_TIME_ZONE = "Asia/Kolkata";

    public static long nowEpoch() {
        return ZonedDateTime.now(ZoneId.of(SYSTEM_TIME_ZONE)).toEpochSecond();
    }

    public static ZonedDateTime now() {
        return ZonedDateTime.now(ZoneId.of(SYSTEM_TIME_ZONE));
    }
}
