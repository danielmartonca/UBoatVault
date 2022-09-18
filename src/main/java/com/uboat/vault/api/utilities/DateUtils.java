package com.uboat.vault.api.utilities;

import java.util.Date;

public class DateUtils {
    public static long getSecondsPassed(Date date) {
        return ((new Date().getTime() - date.getTime()) / 1000);
    }
}
