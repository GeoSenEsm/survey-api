package com.survey.application.dtos.statistics;

/**
 * One (hour-of-day, count) datum used to plot the "Daily stats" hourly
 * line charts. {@code hour} is in the [0, 23] range and interpreted in
 * UTC — same convention as {@link TimeSeriesPointDto}.
 */
public record HourlySeriesPointDto(int hour, long count) {}
