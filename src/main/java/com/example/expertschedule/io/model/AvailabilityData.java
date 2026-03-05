package com.example.expertschedule.io.model;

import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;

/** DTO for planner.domain.time.Availability (extends WeekPeriod). */
@Getter
@Setter
public class AvailabilityData {

    private int calendarWeek;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
}
