package com.example.expertschedule;

import com.example.expertschedule.domain.Expert;
import com.example.expertschedule.domain.Location;
import com.example.expertschedule.domain.Skill;
import com.example.expertschedule.domain.Task;
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
        return factory.forEach(Task.class)
                .filter(task -> {
                    Expert expert = task.getAssignedExpert();
                    Set<Skill> required = task.getRequiredSkills();
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
        return factory.forEach(Task.class)
                .filter(task -> task.getAssignedExpert() != null
                        && task.getCustomer() != null
                        && task.getCustomer().getLocation() != null
                        && task.getAssignedExpert().getBackOfficeLocation() != null)
                .penalize(HardSoftScore.ONE_SOFT,
                        task -> {
                            Location from = task.getAssignedExpert().getBackOfficeLocation();
                            Location to = task.getCustomer().getLocation();
                            return (int) Math.round(from.distanceTo(to));
                        })
                .asConstraint("Minimize travel from expert to customer");
    }
}

