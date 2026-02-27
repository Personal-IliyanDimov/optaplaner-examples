package com.example.expertschedule.planner.cp;

import com.example.expertschedule.planner.domain.Expert;
import com.example.expertschedule.planner.domain.Location;
import com.example.expertschedule.planner.domain.Skill;
import com.example.expertschedule.planner.domain.Order;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;

import java.util.Set;

public class TaskScheduleConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[]{
                missingRequiredSkill(constraintFactory),
                minimizeTravelFromExpertToCustomer(constraintFactory)
        };
    }

    private Constraint missingRequiredSkill(ConstraintFactory factory) {
        // Hard: expert must have all skills required by the order.
        return factory.forEach(Order.class)
                .filter(order -> {
                    Expert expert = order.getAssignedExpert();
                    Set<Skill> required = order.getRequiredSkills();
                    if (expert == null || required == null) {
                        return false;
                    }
                    Set<Skill> expertSkills = expert.getSkills();
                    return expertSkills == null || !expertSkills.containsAll(required);
                })
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Missing required skill");
    }

    private Constraint minimizeTravelFromExpertToCustomer(ConstraintFactory factory) {
        // Soft: minimize distance between expert back office location and customer location.
        return factory.forEach(Order.class)
                .filter(order -> order.getAssignedExpert() != null
                        && order.getCustomer() != null
                        && order.getCustomer().getLocation() != null
                        && order.getAssignedExpert().getBackOfficeLocation() != null)
                .penalize(HardSoftScore.ONE_SOFT,
                        order -> {
                            Location from = order.getAssignedExpert().getBackOfficeLocation();
                            Location to = order.getCustomer().getLocation();
                            return (int) Math.round(from.distanceTo(to));
                        })
                .asConstraint("Minimize travel from expert to customer");
    }
}

