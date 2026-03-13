package org.imd.expertschedule.planner.domain.time;

import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;

@Getter
@Setter
public class TimeSlot {
    private LocalTime startTime;
}
