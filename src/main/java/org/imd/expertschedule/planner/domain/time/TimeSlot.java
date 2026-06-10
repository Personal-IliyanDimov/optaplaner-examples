package org.imd.expertschedule.planner.domain.time;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.optaplanner.core.api.domain.lookup.PlanningId;

import java.time.LocalTime;

@Getter
@Setter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TimeSlot {
    private final LocalTime startTime;

    @PlanningId
    @EqualsAndHashCode.Include
    public LocalTime getPlanningId() {
        return startTime;
    }
}
