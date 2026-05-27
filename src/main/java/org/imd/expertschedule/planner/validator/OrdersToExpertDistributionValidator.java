package org.imd.expertschedule.planner.validator;

import org.imd.expertschedule.planner.domain.Expert;
import org.imd.expertschedule.planner.domain.Order;
import org.imd.expertschedule.planner.solution.ExpertPlanningSolution;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class OrdersToExpertDistributionValidator {

    public void validate(final ExpertPlanningSolution solution,
                         final Collection<Violation> violations) {
        List<Order> orders = solution.getOrderList();
        List<Expert> experts = solution.getExpertList();
        if (orders == null || orders.isEmpty() || experts == null || experts.isEmpty()) {
            return;
        }

        int orderCount = orders.size();
        int expertCount = experts.size();
        double meanUniqueOrdersPerExpert = (double) orderCount / expertCount;

        Map<Expert, Integer> uniqueOrderCountByExpert = new HashMap<>();
        for (Expert expert : experts) {
            uniqueOrderCountByExpert.put(expert, 0);
        }

        for (Order order : orders) {
            List<Expert> capable = new ArrayList<>();
            for (Expert expert : experts) {
                if (expertHasAllRequiredSkills(expert, order)) {
                    capable.add(expert);
                }
            }
            if (capable.size() == 1) {
                Expert sole = capable.getFirst();
                uniqueOrderCountByExpert.merge(sole, 1, Integer::sum);
            }
        }

        for (Expert expert : experts) {
            int uniqueCount = uniqueOrderCountByExpert.getOrDefault(expert, 0);
            if (uniqueCount > meanUniqueOrdersPerExpert) {
                String name = Optional.ofNullable(expert.getName()).orElse("(unnamed)");
                String ref = expert.getId() != null ? expert.getId().toString() : "?";
                violations.add(new Violation(
                        "Violation! Expert " + name + " [" + ref + "] is the only expert who can process "
                                + uniqueCount + " order(s), which exceeds the fair-share mean of "
                                + String.format("%.4f", meanUniqueOrdersPerExpert)
                                + " (= " + orderCount + " orders / " + expertCount + " experts)."));
            }
        }
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
