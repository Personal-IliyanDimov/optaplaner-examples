package com.example.expertschedule.io.model;

import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;

/** DTO matching planner.domain.time.WeekPeriod (calendar week + day + time window). */
@Getter
@Setter
public class WeekPeriodData {

    private int calendarWeek;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
}
