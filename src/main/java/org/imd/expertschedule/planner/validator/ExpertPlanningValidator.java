package org.imd.expertschedule.planner.validator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.imd.expertschedule.planner.solution.ExpertPlanningSolution;

import java.util.Collection;

public class ExpertPlanningValidator {

    public Collection<Violation> validate(ExpertPlanningSolution solution) {
        final Collection<Violation> result = new java.util.ArrayList<>();

        // each expert must have none null availability
        validateExpertAvailabilityIsNotNullOrEmpty(solution, result);

        return result;
    }

    private void validateExpertAvailabilityIsNotNullOrEmpty(final ExpertPlanningSolution solution,
                                                            final Collection<Violation> vc) {
        solution.getExpertList().forEach(expert -> {
            if (expert.getAvailabilities() == null) {
                vc.add(new Violation("Expert " + expert.getId() + " has null availability"));
            }
        });
    }


    @Getter
    @AllArgsConstructor
    public static class Violation {
        private String message;
    }
}
