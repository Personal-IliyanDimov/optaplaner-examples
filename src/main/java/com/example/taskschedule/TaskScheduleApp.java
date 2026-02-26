package com.example.taskschedule;

import java.util.Arrays;
import java.util.List;

import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.termination.TerminationConfig;

public class TaskScheduleApp {

    public static void main(String[] args) {
        List<Employee> employees = Arrays.asList(
                new Employee("Alice"),
                new Employee("Bob")
        );

        List<Task> tasks = Arrays.asList(
                new Task("Task A", 3),
                new Task("Task B", 2),
                new Task("Task C", 5)
        );

        TaskSchedule problem = new TaskSchedule(employees, tasks);

        SolverConfig solverConfig = new SolverConfig()
                .withSolutionClass(TaskSchedule.class)
                .withEntityClasses(Task.class)
                .withConstraintProviderClass(TaskScheduleConstraintProvider.class)
                .withTerminationConfig(new TerminationConfig().withSecondsSpentLimit(5L));

        SolverFactory<TaskSchedule> solverFactory = SolverFactory.create(solverConfig);
        Solver<TaskSchedule> solver = solverFactory.buildSolver();

        TaskSchedule solution = solver.solve(problem);

        System.out.println("Best score: " + solution.getScore());
        for (Task task : solution.getTaskList()) {
            System.out.printf("  %s -> %s%n",
                    task.getName(),
                    task.getAssignedEmployee() == null ? "unassigned" : task.getAssignedEmployee().getName());
        }
    }
}

