package org.imd.expertschedule.planner.util;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class DayInterval {
    private LocalDate date;
    private LocalTime from;
    private LocalTime to;
}
