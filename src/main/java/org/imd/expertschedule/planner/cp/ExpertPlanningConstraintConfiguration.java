package org.imd.expertschedule.planner.cp;

import org.optaplanner.core.api.domain.constraintweight.ConstraintConfiguration;
import org.optaplanner.core.api.domain.constraintweight.ConstraintWeight;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;

@ConstraintConfiguration
public class ExpertPlanningConstraintConfiguration {

    @ConstraintWeight(WeightNames.EA_AVAILABILITY_CONFLICT)
    private HardMediumSoftScore constraintExpertAvailability = HardMediumSoftScore.ONE_HARD;

    @ConstraintWeight(WeightNames.OA_AVAILABILITY_CONFLICT)
    private HardMediumSoftScore constraintOrderAvailability = HardMediumSoftScore.ONE_HARD;

    @ConstraintWeight(WeightNames.ES_VS_OS_SKILL_CONFLICT)
    private HardMediumSoftScore constraintExpertSkillsVsOrderSkills = HardMediumSoftScore.ONE_HARD;

    @ConstraintWeight(WeightNames.O_DUE_DATE_CONFLICT)
    private HardMediumSoftScore constraintOrderDueDate = HardMediumSoftScore.ONE_HARD;

    @ConstraintWeight(WeightNames.FD_PE_SI_CONFLICT)
    private HardMediumSoftScore constraintFairlyDistributePerExpertScheduledItems = HardMediumSoftScore.ONE_MEDIUM;

    public static final class WeightNames {
        public static final String EA_AVAILABILITY_CONFLICT = "Expert Availability Conflict";
        public static final String OA_AVAILABILITY_CONFLICT = "Order Availability Conflict";
        public static final String ES_VS_OS_SKILL_CONFLICT = "ExpertSkills Vs OrderSkills Conflict";
        public static final String O_DUE_DATE_CONFLICT = "OrderDueDate Conflict";
        public static final String FD_PE_SI_CONFLICT = "Fairly Distribute Per Expert Scheduled Items Conflict";
    }
}
