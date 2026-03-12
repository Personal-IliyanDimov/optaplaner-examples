package org.imd.expertschedule.io.model;

import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;

/** DTO for planner.domain.time.Absence (extends WeekPeriod + reason). */
@Getter
@Setter
public class AbsenceData {

    private int calendarWeek;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private String reason;
}
