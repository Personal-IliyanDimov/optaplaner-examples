package org.imd.expertschedule.planner.cp;

import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;

public class ExpertPlanningConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[]{
                matchExpertAvailabilityAndOrderAvailability(constraintFactory),
                matchExpertSkillsAndOrderSkills(constraintFactory),
                matchOrderDueDate(constraintFactory),
                fairlyDistributeScheduledItems(constraintFactory)
        };
    }

    private Constraint matchExpertAvailabilityAndOrderAvailability(ConstraintFactory factory) {

        // hard constraint - hard penalize per (availability difference in hours between expert and order)

        return null;
    }

    private Constraint matchExpertSkillsAndOrderSkills(ConstraintFactory factory) {

        // hard constraint
        // hard penalize - if (order skills - expert skills) >= 0 then hard penalize by number of missing skills
        // soft penalize - if (expert skills - order skills) >= 0 then soft penalize by number of missing skills

        return null;
    }

    private Constraint matchOrderDueDate(ConstraintFactory factory) {

        // hard constraint - scheduled item date must be before order due date.
        // hard penalize - priority status * days delayed

        return null;
    }

    private Constraint fairlyDistributeScheduledItems(ConstraintFactory factory) {

        // medium constraint

        return null;
    }

}
