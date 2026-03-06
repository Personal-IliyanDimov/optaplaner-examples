package com.example.expertschedule.planner.solution;

import com.example.expertschedule.planner.domain.BackOffice;
import com.example.expertschedule.planner.domain.Customer;
import com.example.expertschedule.planner.domain.Expert;
import com.example.expertschedule.planner.domain.Order;
import com.example.expertschedule.planner.domain.Skill;
import com.example.expertschedule.planner.domain.refs.ExpertRef;
import lombok.Getter;
import lombok.Setter;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;

import java.util.List;

@Getter
@Setter
public class SolutionContext {

    private List<Skill> skillList;
    private List<Expert> expertList;
    private List<Order> orderList;
    private List<Customer> customerList;
    private List<BackOffice> backOfficeList;
}
