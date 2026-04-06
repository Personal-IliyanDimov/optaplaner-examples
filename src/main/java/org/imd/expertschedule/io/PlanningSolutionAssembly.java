package org.imd.expertschedule.io;

import org.imd.expertschedule.io.generator.GeneratorConfig;
import org.imd.expertschedule.planner.solution.PlannerParameters;

public final class PlanningSolutionAssembly {

    private PlanningSolutionAssembly() {}

    public static PlannerParameters plannerParametersFromMetadata(final GeneratorConfig metadata) {
        final PlannerParameters plannerParameters = new PlannerParameters();
        if (metadata.getYear() <= 0) {
            throw new IllegalArgumentException("Invalid year in metadata: " + metadata.getYear());
        }
        if ((metadata.getCalendarWeek() <= 0) || (metadata.getCalendarWeek() > 53)) {
            throw new IllegalArgumentException("Invalid calendar week in metadata: " + metadata.getCalendarWeek());

        }
        if (metadata.getWeekWorkingDays() == null || metadata.getWeekWorkingDays().length == 0) {
            throw new IllegalArgumentException("Invalid week working days in metadata: " + metadata.getWeekWorkingDays());
        }

        plannerParameters.getPlannerRelated().setYear(metadata.getYear());
        plannerParameters.getPlannerRelated().setCalendarWeek(metadata.getCalendarWeek());
        plannerParameters.getPlannerRelated().setWorkingDays(metadata.getWeekWorkingDays());
        return plannerParameters;
    }
}
