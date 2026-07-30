package com.survey.application.dtos.statistics;

import java.time.LocalDate;

/** One (UTC-day, count) datum used to plot line charts. */
public record TimeSeriesPointDto(LocalDate date, long count) {}
