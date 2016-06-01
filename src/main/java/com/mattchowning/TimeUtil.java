package com.mattchowning;

public class TimeUtil {

    private static final long SEC_SINCE_1900 = 2208988800L;
    private static final long MILLI_IN_SECOND = 1000L;


    public static long unixFromTimeSince1900(long t) {
        return (t - SEC_SINCE_1900) * MILLI_IN_SECOND;
    }

    public static long getCurrentTimeSince1900() {
        return System.currentTimeMillis() / MILLI_IN_SECOND + SEC_SINCE_1900;
    }
}
