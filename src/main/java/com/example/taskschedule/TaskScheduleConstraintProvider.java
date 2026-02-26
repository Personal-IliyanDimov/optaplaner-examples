package com.example.taskschedule;

import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;

public class TaskScheduleConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[]{
                balanceWorkload(constraintFactory)
        };
    }

    private Constraint balanceWorkload(ConstraintFactory factory) {
        // Very simple example:
        // Minimize total task duration per employee, encouraging OptaPlanner to distribute tasks.
        return factory.from(Task.class)
                .groupBy(Task::getAssignedEmployee,
                        org.optaplanner.core.api.score.stream.ConstraintCollectors.sum(Task::getDurationInHours))
                .penalize(HardSoftScore.ONE_SOFT,
                        (employee, totalDuration) -> totalDuration)
                .asConstraint("Balance workload");
    }
}

