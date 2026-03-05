package com.example.expertschedule.planner.solution;

import com.example.expertschedule.planner.domain.BackOffice;
import com.example.expertschedule.planner.domain.Customer;
import com.example.expertschedule.planner.domain.Expert;
import com.example.expertschedule.planner.domain.ExpertSchedule;
import com.example.expertschedule.planner.domain.Order;
import com.example.expertschedule.planner.domain.Skill;
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

    @ProblemFactCollectionProperty
    private List<Skill> skillList;

    @ValueRangeProvider(id = "expertRange")
    @ProblemFactCollectionProperty
    private List<Expert> expertList;

    @ProblemFactCollectionProperty
    private List<BackOffice> backOfficeList;

    @ProblemFactCollectionProperty
    private List<Customer> customerList;

    @ProblemFactCollectionProperty
    private List<Order> orderList;

    @PlanningEntityCollectionProperty
    private List<ExpertSchedule> expertScheduleList;

    @PlanningScore
    private HardSoftScore score;
}
