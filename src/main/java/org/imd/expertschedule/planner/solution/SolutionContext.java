package org.imd.expertschedule.planner.solution;

import org.imd.expertschedule.planner.domain.BackOffice;
import org.imd.expertschedule.planner.domain.Customer;
import org.imd.expertschedule.planner.domain.Expert;
import org.imd.expertschedule.planner.domain.Order;
import org.imd.expertschedule.planner.domain.Skill;
import lombok.Getter;
import lombok.Setter;

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
