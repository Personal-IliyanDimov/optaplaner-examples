package org.imd.expertschedule.planner.domain.time;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalTime;

@Getter
@Setter
@RequiredArgsConstructor
@ToString
public class TimeSlot {
    private final LocalTime startTime;
}
