package org.imd.expertschedule.planner.domain;

import lombok.EqualsAndHashCode;
import org.imd.expertschedule.planner.domain.refs.ExpertRef;
import lombok.Getter;
import lombok.Setter;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class ExpertSchedule {
    private Expert expert;
    private LocalDate date;
}

