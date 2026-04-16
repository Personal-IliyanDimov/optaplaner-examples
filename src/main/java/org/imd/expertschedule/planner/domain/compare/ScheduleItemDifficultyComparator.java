package org.imd.expertschedule.planner.domain.compare;

import org.imd.expertschedule.planner.domain.Order;
import org.imd.expertschedule.planner.domain.OrderPriority;
import org.imd.expertschedule.planner.domain.ScheduleItem;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Set;

public final class ScheduleItemDifficultyComparator implements Comparator<ScheduleItem> {

    private final Comparator<ScheduleItem> siComparator = new InternalComparator();

    @Override
    public int compare(ScheduleItem a, ScheduleItem b) {
        return Comparator.nullsFirst(siComparator).compare(a, b);
    }

    private static class InternalComparator implements Comparator<ScheduleItem> {

        @Override
        public int compare(ScheduleItem si1, ScheduleItem si2) {
            final Order order1 = si1.getOrder();
            final Order order2 = si2.getOrder();

            int result = Comparator
                    .nullsFirst(Comparator.comparingLong(this::orderPriorityLevel))
                    .compare(order1, order2);

            if  (result != 0) {
                return result;
            }

            result = Comparator
                    .nullsFirst(LocalDate::compareTo)
                    .compare(order1.getDueDate(), order2.getDueDate());

            if  (result != 0) {
                return result;
            }

            result = Comparator
                    .nullsFirst(Comparator.comparing(Set<?>::size))
                    .compare(order1.getRequiredSkills(), order2.getRequiredSkills());

            if  (result != 0) {
                return result;
            }

            result = Comparator
                    .nullsFirst(Duration::compareTo)
                    .compare(order1.getDiagnosisDuration(), order2.getDiagnosisDuration());

            return result;
        }

        private int orderPriorityLevel(final Order order) {
            final OrderPriority op = order.getPriority();
            final int result = op == null ? 0 : op.getLevel();
            return result;
        }
    }
}
