package com.turkcell.roaming.roaming_assistant.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
public class DateUtil {
    public static int daysInclusive(LocalDate start, LocalDate end){
        return (int) ChronoUnit.DAYS.between(start, end) + 1;
    }
}