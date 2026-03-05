package com.example.expertschedule.planner.cp;

import com.example.expertschedule.planner.domain.Expert;
import com.example.expertschedule.planner.domain.ExpertSchedule;
import com.example.expertschedule.planner.domain.Location;
import com.example.expertschedule.planner.domain.ScheduleItem;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.score.stream.Joiners;

import java.util.Collections;
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
        return factory.forEach(ExpertSchedule.class)
                .expand(schedule -> schedule.getItems() != null ? schedule.getItems() : Collections.emptyList())
                .join(Expert.class, Joiners.equal((schedule, item) -> schedule.getExpertRef(), Expert::getId))
                .filter((schedule, item, expert) -> item.getOrder() != null
                        && item.getOrder().getRequiredSkills() != null
                        && (expert.getSkills() == null || !expert.getSkills().containsAll(item.getOrder().getRequiredSkills())))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Missing required skill");
    }

    private Constraint minimizeTravelFromExpertToCustomer(ConstraintFactory factory) {
        return factory.forEach(ExpertSchedule.class)
                .expand(schedule -> schedule.getItems() != null ? schedule.getItems() : Collections.emptyList())
                .join(Expert.class, Joiners.equal((schedule, item) -> schedule.getExpertRef(), Expert::getId))
                .filter((schedule, item, expert) -> item.getOrder() != null
                        && item.getOrder().getLocation() != null
                        && expert.getBackOfficeLocation() != null)
                .penalize(HardSoftScore.ONE_SOFT,
                        (schedule, item, expert) -> {
                            Location from = expert.getBackOfficeLocation();
                            Location to = item.getOrder().getLocation();
                            return (int) Math.round(from.distanceTo(to));
                        })
                .asConstraint("Minimize travel from expert to customer");
    }
}
