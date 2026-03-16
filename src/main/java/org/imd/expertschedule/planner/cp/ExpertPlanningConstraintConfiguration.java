package org.imd.expertschedule.planner.cp;

import org.optaplanner.core.api.domain.constraintweight.ConstraintConfiguration;
import org.optaplanner.core.api.domain.constraintweight.ConstraintWeight;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;

@ConstraintConfiguration
public class ExpertPlanningConstraintConfiguration {

    @ConstraintWeight(WeightNames.EA_VS_OA_AVAILABILITY_CONFLICT)
    private HardMediumSoftScore constraintExpertAvailabilityVsOrderAvailability = HardMediumSoftScore.ONE_HARD;

    @ConstraintWeight(WeightNames.ES_VS_OS_SKILL_CONFLICT)
    private HardMediumSoftScore constraintExpertSkillsVsOrderSkills = HardMediumSoftScore.ONE_HARD;

    @ConstraintWeight(WeightNames.O_DUE_DATE_CONFLICT)
    private HardMediumSoftScore constraintOrderDueDate = HardMediumSoftScore.ONE_HARD;

    @ConstraintWeight(WeightNames.FD_SI_CONFLICT)
    private HardMediumSoftScore constraintFairlyDistributeScheduledItems = HardMediumSoftScore.ONE_MEDIUM;

    public static final class WeightNames {
        private static final String EA_VS_OA_AVAILABILITY_CONFLICT = "ExpertAvailability Vs OrderAvailability";
        private static final String ES_VS_OS_SKILL_CONFLICT = "ExpertSkills Vs OrderSkills";
        private static final String O_DUE_DATE_CONFLICT = "OrderDueDate";
        private static final String FD_SI_CONFLICT = "Fairly Distribute Scheduled Items";
    }
}
