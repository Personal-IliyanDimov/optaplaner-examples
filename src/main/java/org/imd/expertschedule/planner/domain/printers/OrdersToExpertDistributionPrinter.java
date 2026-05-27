package org.imd.expertschedule.planner.domain.printers;

import org.imd.expertschedule.planner.domain.Expert;
import org.imd.expertschedule.planner.domain.Order;
import org.imd.expertschedule.planner.solution.ExpertPlanningSolution;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class OrdersToExpertDistributionPrinter {

    public void print(ExpertPlanningSolution solution) {
        print(solution, System.out);
    }

    public void print(ExpertPlanningSolution solution, PrintStream out) {
        out.println("========== Orders each expert can process (required skills) ==========");

        List<Order> orders = solution.getOrderList();
        List<Expert> experts = solution.getExpertList();

        if (orders == null || orders.isEmpty()) {
            out.println("(no orders in solution)");
            out.println();
            return;
        }
        if (experts == null || experts.isEmpty()) {
            out.println("(no experts in solution)");
            out.println();
            return;
        }

        int orderCount = orders.size();
        Map<Expert, Integer> processableCountByExpert = new HashMap<>();

        for (Order order : orders) {
            List<Expert> capable = new ArrayList<>();

            for (Expert expert : experts) {
                if (expertHasAllRequiredSkills(expert, order)) {
                    capable.add(expert);
                }
            }

            if (capable.size() == 1) {
                processableCountByExpert.merge(capable.getFirst(), 1, Integer::sum);
            }
        }

        out.printf("Each order is counted at most once per expert if that expert's skills cover the order's required skills.%n");
        out.printf("Total orders: %d%n%n", orderCount);

        for (Expert expert : experts) {
            int count = processableCountByExpert.getOrDefault(expert, 0);
            double percent = 100.0 * count / orderCount;
            String name = expert.getName();
            String ref = expert.getId().toString();
            out.printf("  %s [%s]: %d / %d orders (%.2f%%)%n", name, ref, count, orderCount, percent);
        }
        out.println();
    }

    private static boolean expertHasAllRequiredSkills(Expert expert, Order order) {
        if (order.getRequiredSkills() == null) {
            return true;
        }
        if (expert.getSkills() == null) {
            return false;
        }
        return expert.getSkills().containsAll(order.getRequiredSkills());
    }
}
