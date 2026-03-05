package com.example.expertschedule;

import com.example.expertschedule.loader.ExpertPlanningSolutionLoader;
import com.example.expertschedule.planner.cp.TaskScheduleConstraintProvider;
import com.example.expertschedule.planner.domain.Order;
import com.example.expertschedule.planner.solution.ExpertPlanningSolution;
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
                .withEntityClasses(Order.class)
                .withConstraintProviderClass(TaskScheduleConstraintProvider.class)
                .withTerminationConfig(new TerminationConfig().withSecondsSpentLimit(5L));

        SolverFactory<ExpertPlanningSolution> solverFactory = SolverFactory.create(solverConfig);
        Solver<ExpertPlanningSolution> solver = solverFactory.buildSolver();

        ExpertPlanningSolution solution = solver.solve(problem);

        System.out.println("Best score: " + solution.getScore());
        for (Order order : solution.getOrderList()) {
            System.out.printf("  %s -> %s at %s%n",
                    order.getCode(),
                    order.getAssignedExpert() == null ? "unassigned" : order.getAssignedExpert().getName(),
                    order.getCustomer() == null ? "n/a" : order.getCustomer().getName());
        }
    }
}
