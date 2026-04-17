package org.imd.expertschedule.planner.validator;


import org.imd.expertschedule.planner.analyzer.DistributionAnalyzer;
import org.imd.expertschedule.planner.analyzer.SkillDistribution;
import org.imd.expertschedule.planner.solution.ExpertPlanningSolution;
import org.imd.expertschedule.planner.solution.PlannerParameters;
import org.imd.expertschedule.planner.util.PlannerHelper;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class SkillsSupplyAndDemandValidator {

    private final DistributionAnalyzer distributionAnalyzer = new DistributionAnalyzer();

    public void validate(final ExpertPlanningSolution solution,
                         final PlannerParameters plannerParameters,
                         final PlannerHelper helper,
                         final Collection<Violation> violations) {

        final List<SkillDistribution> demandedSkills = distributionAnalyzer.orderSkillDistributionBasedOnDueDate(solution);
        final List<SkillDistribution> suppliedSkills = distributionAnalyzer.expertSkillDistributionBasedOnDueDate(solution);


    }
    }


}
