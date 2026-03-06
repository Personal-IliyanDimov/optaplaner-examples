package com.example.expertschedule.planner.solution;

import com.example.expertschedule.planner.domain.Expert;
import com.example.expertschedule.planner.domain.ExpertSchedule;
import com.example.expertschedule.planner.domain.Order;
import com.example.expertschedule.planner.domain.Skill;
import com.example.expertschedule.planner.domain.refs.ExpertRef;
import com.example.expertschedule.planner.domain.refs.OrderRef;
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
public class ExpertPlanningSolution {

    private SolutionContext context;

    @ValueRangeProvider(id = "expertRefRange")
    @ProblemFactCollectionProperty
    private List<ExpertRef> expertRefList;

    @ValueRangeProvider(id = "orderRefRange")
    @ProblemFactCollectionProperty
    private List<OrderRef> orderRefList;

    @PlanningEntityCollectionProperty
    private List<ExpertSchedule> expertScheduleList;

    @PlanningScore
    private HardSoftScore score;
}
