package org.imd.expertschedule.io.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class TimeSlotData {
    private LocalTime startTime;
}
