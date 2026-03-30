package org.imd.expertschedule.planner.domain.time;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class WeekPeriod {
    private int year;
    private int calendarWeek;
    private int workDay;
    private LocalTime startTime;
    private LocalTime endTime;
}
