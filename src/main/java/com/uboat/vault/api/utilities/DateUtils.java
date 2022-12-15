package com.uboat.vault.api.utilities;

import java.util.Date;

public class DateUtils {
    /**
     * Seconds passed since the date given asa parameter.
     */
    public static long getSecondsPassed(Date date) {
        return getSecondsPassed(date, new Date());
    }

    /**
     * Seconds passed between startDate and endDate.
     */
    public static long getSecondsPassed(Date startDate, Date endDate) {
        return ((endDate.getTime() - startDate.getTime()) / 1000);
    }

}
