package org.imd.expertschedule;

import org.imd.expertschedule.io.loader.ExpertPlanningSolutionLoader;
import org.imd.expertschedule.planner.cp.ExpertPlanningConstraintProvider;
import org.imd.expertschedule.planner.domain.ExpertSchedule;
import org.imd.expertschedule.planner.domain.ScheduleItem;
import org.imd.expertschedule.planner.domain.refs.OrderRef;
import org.imd.expertschedule.planner.solution.ExpertPlanningSolution;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.termination.TerminationConfig;

import java.io.IOException;
import java.nio.file.Path;

public class TaskScheduleApp {

    public static void main(String[] args) throws IOException {
        Path dataDir = Path.of("data");
        ExpertPlanningSolutionLoader loader = new ExpertPlanningSolutionLoader();
        ExpertPlanningSolution problem = loader.loadFromDirectory(dataDir);

        SolverConfig solverConfig = new SolverConfig()
                .withSolutionClass(ExpertPlanningSolution.class)
                .withEntityClasses(ExpertSchedule.class)
                .withConstraintProviderClass(ExpertPlanningConstraintProvider.class)
                .withTerminationConfig(new TerminationConfig().withSecondsSpentLimit(15L));

        SolverFactory<ExpertPlanningSolution> solverFactory = SolverFactory.create(solverConfig);
        Solver<ExpertPlanningSolution> solver = solverFactory.buildSolver();

        ExpertPlanningSolution solution = solver.solve(problem);

        System.out.println("Best score: " + solution.getScore());
        for (ExpertSchedule schedule : solution.getExpertScheduleList()) {
            System.out.printf("Expert %d on %s: %d items%n",
                    schedule.getExpertRef().getId(),
                    schedule.getDate(),
                    schedule.getItems() != null ? schedule.getItems().size() : 0);
            if (schedule.getItems() != null) {
                for (ScheduleItem item : schedule.getItems()) {
                    OrderRef order = item.getOrderRef();
                    if (order != null) {
                        System.out.println("OrderRef " + order.getId() + "Travel: " + item.getTravelDuration() +
                                " Slot: W" + item.getSlot().getCalendarWeek() + " Day: " + item.getSlot().getDayOfWeek() +
                                "[" + item.getSlot().getStartTime() + " - " + item.getSlot().getEndTime() + "]");
                    }
                }
            }
        }
    }
}
