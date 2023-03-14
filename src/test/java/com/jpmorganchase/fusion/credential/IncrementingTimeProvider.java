package com.jpmorganchase.fusion.credential;

import java.time.*;
import java.util.concurrent.atomic.AtomicLong;

public class IncrementingTimeProvider implements TimeProvider {

    private final int year;
    private final int month;
    private final int day;
    private final int hour;
    private final int minute;
    private final int seconds;

    private final AtomicLong additionalSeconds = new AtomicLong(0L);

    public IncrementingTimeProvider(int year, int month, int day, int hour, int minute, int seconds) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.seconds = seconds;
    }

    public void increment(long seconds) {
        additionalSeconds.addAndGet(seconds);
    }

    @Override
    public long currentTimeMillis() {
        LocalDate date = LocalDate.of(year, month, day);
        LocalTime time = LocalTime.of(hour, minute, seconds);
        ZonedDateTime zdt = ZonedDateTime.of(date, time, ZoneId.of("UTC"));
        ZonedDateTime updated = zdt.plusSeconds(additionalSeconds.get());
        return updated.toInstant().toEpochMilli();
    }
}
