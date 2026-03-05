package com.example.expertschedule.io.model;

import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Getter
@Setter
public class AvailabilityData {

    /** Day of week (e.g. MONDAY). */
    private DayOfWeek dayOfWeek;
    /** Start of availability window. */
    private LocalTime startTime;
    /** End of availability window. */
    private LocalTime endTime;
}
