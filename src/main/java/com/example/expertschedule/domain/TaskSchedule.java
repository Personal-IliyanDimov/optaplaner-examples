package com.example.expertschedule.domain;

import lombok.Getter;
import lombok.Setter;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

import java.util.List;

@Getter
@Setter
@PlanningSolution
public class TaskSchedule {

    @ProblemFactCollectionProperty
    private List<Skill> skillList;

    @ValueRangeProvider(id = "expertRange")
    @ProblemFactCollectionProperty
    private List<Expert> expertList;

    @ProblemFactCollectionProperty
    private List<Customer> customerList;

    @PlanningEntityCollectionProperty
    private List<Task> taskList;

    @PlanningScore
    private HardSoftScore score;

}
