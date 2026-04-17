package org.imd.expertschedule.planner.validator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.imd.expertschedule.planner.analyzer.DistributionAnalyzer;
import org.imd.expertschedule.planner.domain.Expert;
import org.imd.expertschedule.planner.domain.Order;
import org.imd.expertschedule.planner.domain.Skill;
import org.imd.expertschedule.planner.domain.time.Availability;
import org.imd.expertschedule.planner.solution.ExpertPlanningSolution;
import org.imd.expertschedule.planner.solution.PlannerParameters;
import org.imd.expertschedule.planner.util.PlannerHelper;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class PlanningSolutionValidator {

    private final PlannerParameters plannerParameters;
    private final PlannerHelper helper = new PlannerHelper();


    public Collection<Violation> validate(final ExpertPlanningSolution solution,
                                          final Collection<Violation> violations) {

        // each expert must have none null availability
        validateExpertAvailabilityIsNotNullOrEmpty(solution, violations);
        validateExpertAvailabilitySlotsAreValid(solution, violations);
        validateOrderCustomerAvailabilitySlotsAreValid(solution, violations);

        final SkillsSupplyAndDemandValidator sdValidator = new SkillsSupplyAndDemandValidator(new DistributionAnalyzer(helper));
        sdValidator.validate(solution, violations);

        return violations;
    }

    private void validateExpertAvailabilityIsNotNullOrEmpty(final ExpertPlanningSolution solution,
                                                            final Collection<Violation> vc) {
        solution.getExpertList().forEach(expert -> {
            if (expert.getAvailabilities() == null) {
                vc.add(new Violation("Expert " + expert.getId() + " has null availability"));
            }
        });
    }

    private void validateExpertAvailabilitySlotsAreValid(final ExpertPlanningSolution solution,
                                                         final Collection<Violation> result) {
        for (Expert expert : solution.getExpertList()) {
            if (expert.getAvailabilities() != null) {
                for (Availability availability : expert.getAvailabilities()) {
                    if (! helper.lessOrEqual(availability.getStartTime(), (availability.getEndTime()))) {
                        result.add(new Violation("Expert " + expert.getId() + " has invalid availability slot: start is after end."));
                    }
                }

                for (Availability availability : expert.getAvailabilities()) {
                    if (! helper.lessOrEqual(plannerParameters.getExpertRelated().getWorkingDayStartTime(), availability.getStartTime())) {
                        result.add(new Violation("Expert " + expert.getId() + " has invalid availability start time."));
                    }

                    if (! helper.lessOrEqual(availability.getEndTime(), plannerParameters.getExpertRelated().getWorkingDayEndTime())) {
                        result.add(new Violation("Expert " + expert.getId() + " has invalid availability end time."));
                    }
                }
            }
        }
    }


    private void validateOrderCustomerAvailabilitySlotsAreValid(final ExpertPlanningSolution solution,
                                                               final Collection<Violation> result) {
        for (Order order : solution.getOrderList()) {
            if (order.getCustomerAvailabilities() != null) {
                for (Availability availability : order.getCustomerAvailabilities()) {
                    if (! helper.lessOrEqual(availability.getStartTime(), (availability.getEndTime()))) {
                        result.add(new Violation("Order " + order.getId() + " has invalid availability slot: start is after end."));
                    }
                }

                for (Availability availability : order.getCustomerAvailabilities()) {
                    if (! helper.lessOrEqual(plannerParameters.getExpertRelated().getWorkingDayStartTime(), availability.getStartTime())) {
                        result.add(new Violation("Order " + order.getId() + " has invalid availability start time."));
                    }

                    if (! helper.lessOrEqual(availability.getEndTime(), plannerParameters.getExpertRelated().getWorkingDayEndTime())) {
                        result.add(new Violation("Order " + order.getId() + " has invalid availability end time."));
                    }
                }
            }
        }
    }
}
