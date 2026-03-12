package org.imd.expertschedule.planner.cp;

import org.optaplanner.core.api.domain.constraintweight.ConstraintConfiguration;
import org.optaplanner.core.api.domain.constraintweight.ConstraintWeight;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;

@ConstraintConfiguration
public class ExpertPlanningConstraintConfiguration {


    @ConstraintWeight("Speaker conflict")
    private HardMediumSoftScore speakerConflict = null;

    @ConstraintWeight("Theme track conflict")
    private HardMediumSoftScore themeTrackConflict = null;

    @ConstraintWeight("Content conflict")
    private HardMediumSoftScore contentConflict = null;

    public static final class WeightNames {
        private static final String A_CONFLICT = "A_CONFLICT";
        private static final String B_CONFLICT = "B_CONFLICT";
        private static final String C_CONFLICT = "C_CONFLICT";
    }
}
