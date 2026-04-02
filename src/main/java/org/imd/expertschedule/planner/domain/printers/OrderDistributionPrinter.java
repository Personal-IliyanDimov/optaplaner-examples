package org.imd.expertschedule.planner.domain.printers;

import org.imd.expertschedule.planner.domain.Order;
import org.imd.expertschedule.planner.domain.ScheduleItem;
import org.imd.expertschedule.planner.domain.Skill;
import org.imd.expertschedule.planner.solution.ExpertPlanningSolution;

import java.io.PrintStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

public final class OrderDistributionPrinter {

    public void print(ExpertPlanningSolution solution) {
        print(solution, System.out);
    }

    public void print(ExpertPlanningSolution solution, PrintStream out) {
        final List<ScheduleItem> siList = solution.getScheduleItemList();

        final NavigableMap<LocalDate, Set<Order>> scheduledOrdersByDay = new TreeMap<>();
        final List<Order> skippedOrders = new ArrayList<>();

        for (ScheduleItem item : siList) {
            Order order = item.getOrder();

            if (!isScheduled(item)) {
                skippedOrders.add(item.getOrder());
            } else {
                final LocalDate date = item.getExpertSchedule().getDate();
                scheduledOrdersByDay.computeIfAbsent(date, d -> new HashSet<>()).add(order);
            }
        }

        out.println("========== Orders per working day (scheduled assignments) ==========");
        if (scheduledOrdersByDay.isEmpty()) {
            out.println("(no scheduled items)");
            out.println();
            return;
        }

        for (Map.Entry<LocalDate, Set<Order>> entry : scheduledOrdersByDay.entrySet()) {
            LocalDate date = entry.getKey();
            Set<Order> dayOrders = entry.getValue();
            out.println();
            out.printf("%s (%s): %d order(s)%n", date, date.getDayOfWeek(), dayOrders.size());

            NavigableMap<String, Integer> skillCounts = skillOrderCounts(dayOrders);
            out.println("  Skill distribution (orders requiring each skill):");
            if (skillCounts.isEmpty()) {
                out.println("    (no required skills on these orders)");
            } else {
                for (Map.Entry<String, Integer> sk : skillCounts.entrySet()) {
                    out.printf("    %s: %d%n", sk.getKey(), sk.getValue());
                }
            }
        }
        out.println();
    }

    private boolean isScheduled(ScheduleItem item) {
        return item.getExpertSchedule() != null && item.getTimeSlot() != null;
    }

    private NavigableMap<String, Integer> skillOrderCounts(Set<Order> dayOrders) {
        NavigableMap<String, Integer> counts = new TreeMap<>();
        for (Order order : dayOrders) {
            Set<Skill> required = order.getRequiredSkills();
            if (required != null) {
                for (Skill skill : required) {
                    String name = skill.getName();
                    counts.merge(name, 1, Integer::sum);
                }
            }
        }
        return counts;
    }
}
