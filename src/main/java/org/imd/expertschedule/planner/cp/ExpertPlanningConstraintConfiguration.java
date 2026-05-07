package org.imd.expertschedule.planner.cp;

import org.optaplanner.core.api.domain.constraintweight.ConstraintConfiguration;
import org.optaplanner.core.api.domain.constraintweight.ConstraintWeight;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;

@ConstraintConfiguration
public class ExpertPlanningConstraintConfiguration {

    @ConstraintWeight(WeightNames.EA_AVAILABILITY_CONFLICT)
    private HardMediumSoftScore constraintExpertAvailability = HardMediumSoftScore.ONE_HARD;

    @ConstraintWeight(WeightNames.EA_LUNCH_TIME_CONFLICT)
    private HardMediumSoftScore constraintExpertAvailabilityLunchTime = HardMediumSoftScore.ONE_HARD;

    @ConstraintWeight(WeightNames.OVERLAPS_WITH_OTHER_MEETING_CONFLICT)
    private HardMediumSoftScore constraintOverlapsWithOtherMeeting = HardMediumSoftScore.ONE_HARD;

    @ConstraintWeight(WeightNames.OA_AVAILABILITY_CONFLICT)
    private HardMediumSoftScore constraintOrderAvailability = HardMediumSoftScore.ONE_HARD;

    @ConstraintWeight(WeightNames.ES_VS_OS_SKILL_CONFLICT)
    private HardMediumSoftScore constraintExpertSkillsVsOrderSkills = HardMediumSoftScore.ONE_HARD;

    @ConstraintWeight(WeightNames.O_DUE_DATE_CONFLICT)
    private HardMediumSoftScore constraintOrderDueDate = HardMediumSoftScore.ONE_HARD;

    @ConstraintWeight(WeightNames.EL_TD_PD_CONFLICT)
    private HardMediumSoftScore constraintExpertLimitTravelDistancePerDay = HardMediumSoftScore.ONE_HARD;

    @ConstraintWeight(WeightNames.FD_PE_PP_SI_CONFLICT)
    private HardMediumSoftScore constraintFairlyDistributePerExpertPerPeriodScheduledItems = HardMediumSoftScore.ONE_SOFT;

    @ConstraintWeight(WeightNames.FD_PE_PD_SI_CONFLICT)
    private HardMediumSoftScore constraintFairlyDistributePerExpertPerDayScheduledItems = HardMediumSoftScore.ONE_SOFT;

    public static final class WeightNames {
        public static final String EA_AVAILABILITY_CONFLICT = "Expert Availability Conflict";
        public static final String EA_LUNCH_TIME_CONFLICT = "Expert Availability Lunch Time Conflict";
        public static final String OVERLAPS_WITH_OTHER_MEETING_CONFLICT = "Overlap With Other Meeting Conflict";
        public static final String OA_AVAILABILITY_CONFLICT = "Order Availability Conflict";
        public static final String ES_VS_OS_SKILL_CONFLICT = "ExpertSkills Vs OrderSkills Conflict";
        public static final String O_DUE_DATE_CONFLICT = "OrderDueDate Conflict";

        public static final String ER_WT_TO_TT_CONFLICT = "Expert Ratio - Work Time To Travel Time";
        public static final String EL_TD_PD_CONFLICT = "Expert Limit - Travel Distance Per Day";

        public static final String FD_PE_PP_SI_CONFLICT = "Fairly Distribute Per Expert Per Period Scheduled Items Conflict";
        public static final String FD_PE_PD_SI_CONFLICT = "Fairly Distribute Per Expert Per Day Scheduled Items Conflict";
    }
}
