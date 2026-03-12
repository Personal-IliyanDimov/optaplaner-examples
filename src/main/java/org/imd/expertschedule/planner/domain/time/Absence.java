package org.imd.expertschedule.planner.domain.time;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Absence extends WeekPeriod {
    private String reason;
}
