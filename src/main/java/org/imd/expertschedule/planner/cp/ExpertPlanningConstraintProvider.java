package org.imd.expertschedule.planner.cp;

import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;

public class ExpertPlanningConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[]{
                missingRequiredSkill(constraintFactory),
                minimizeTravelFromExpertToCustomer(constraintFactory)
        };
    }

    private Constraint missingRequiredSkill(ConstraintFactory factory) {
        return null;
    }

    private Constraint minimizeTravelFromExpertToCustomer(ConstraintFactory factory) {
        return null;

        //        return factory.forEachUniquePair(...)
        //                .
        //                .penalizeConfigurable(ExpertPlanningConstraintConfiguration.WeightNames.A_CONFLICT, ...);
    }
}
