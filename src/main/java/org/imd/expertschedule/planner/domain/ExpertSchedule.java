package org.imd.expertschedule.planner.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.optaplanner.core.api.domain.lookup.PlanningId;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ExpertSchedule {
    private Expert expert;
    private LocalDate date;

    @PlanningId
    @EqualsAndHashCode.Include
    public String getPlanningId() {
        if (expert == null || expert.getId() == null || date == null) {
            return null;
        }
        return expert.getId().getId() + "_" + date;
    }
}

