package org.imd.expertschedule.planner.validator;

import org.imd.expertschedule.planner.domain.Expert;
import org.imd.expertschedule.planner.domain.Location;
import org.imd.expertschedule.planner.domain.Order;
import org.imd.expertschedule.planner.domain.OrderPriority;
import org.imd.expertschedule.planner.domain.Skill;
import org.imd.expertschedule.planner.domain.refs.ExpertRef;
import org.imd.expertschedule.planner.domain.refs.OrderRef;
import org.imd.expertschedule.planner.domain.time.Availability;
import org.imd.expertschedule.planner.solution.ExpertPlanningSolution;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrdersToExpertDistributionValidatorTest {

    private static final LocalDate DUE = LocalDate.of(2026, 1, 12);

    @Test
    void validate_whenEmptyOrderList_addsNoViolations() {
        ExpertPlanningSolution solution = new ExpertPlanningSolution();
        solution.setOrderList(List.of());
        solution.setExpertList(List.of(expert(1L, "A", skill("x"))));

        ArrayList<Violation> violations = new ArrayList<>();
        new OrdersToExpertDistributionValidator().validate(solution, violations);

        assertEquals(0, violations.size());
    }

    @Test
    void validate_whenNoOrderIsUniqueToSingleExpert_addsNoViolations() {
        Skill s = skill("S");
        Expert e1 = expert(1L, "E1", s);
        Expert e2 = expert(2L, "E2", s);
        Expert e3 = expert(3L, "E3", s);

        List<Order> orders = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            orders.add(order(i + 1L, s));
        }

        ExpertPlanningSolution solution = new ExpertPlanningSolution();
        solution.setOrderList(orders);
        solution.setExpertList(List.of(e1, e2, e3));

        ArrayList<Violation> violations = new ArrayList<>();
        new OrdersToExpertDistributionValidator().validate(solution, violations);

        assertEquals(0, violations.size());
    }

    @Test
    void validate_whenExpertHasMoreUniqueOrdersThanMean_addsViolationForThatExpert() {
        Skill s1 = skill("S1");
        Skill s2 = skill("S2");
        Skill s3 = skill("S3");
        Expert e1 = expert(1L, "E1", s1);
        Expert e2 = expert(2L, "E2", s2);
        Expert e3 = expert(3L, "E3", s3);

        List<Order> orders = new ArrayList<>();
        long id = 1L;
        id = addOrders(orders, id, 4, s1);
        id = addOrders(orders, id, 2, s2);
        addOrders(orders, id, 3, s3);

        ExpertPlanningSolution solution = new ExpertPlanningSolution();
        solution.setOrderList(orders);
        solution.setExpertList(List.of(e1, e2, e3));

        ArrayList<Violation> violations = new ArrayList<>();
        new OrdersToExpertDistributionValidator().validate(solution, violations);

        assertEquals(1, violations.size());
        assertTrue(violations.getFirst().getMessage().contains("E1"));
    }

    @Test
    void validate_whenUniqueOrdersPerExpertAtMostMean_addsNoViolations() {
        Skill s1 = skill("S1");
        Skill s2 = skill("S2");
        Skill s3 = skill("S3");
        Expert e1 = expert(1L, "E1", s1);
        Expert e2 = expert(2L, "E2", s2);
        Expert e3 = expert(3L, "E3", s3);

        List<Order> orders = new ArrayList<>();
        long id = 1L;
        id = addOrders(orders, id, 3, s1);
        id = addOrders(orders, id, 3, s2);
        addOrders(orders, id, 3, s3);

        ExpertPlanningSolution solution = new ExpertPlanningSolution();
        solution.setOrderList(orders);
        solution.setExpertList(List.of(e1, e2, e3));

        ArrayList<Violation> violations = new ArrayList<>();
        new OrdersToExpertDistributionValidator().validate(solution, violations);

        assertEquals(0, violations.size());
    }

    private static long addOrders(List<Order> orders, long startId, int n, Skill required) {
        long id = startId;
        for (int i = 0; i < n; i++) {
            orders.add(order(id++, required));
        }
        return id;
    }

    private static Skill skill(String name) {
        Skill s = new Skill();
        s.setName(name);
        return s;
    }

    private static Expert expert(long expertId, String name, Skill... skills) {
        Expert expert = new Expert();
        ExpertRef ref = new ExpertRef();
        ref.setId(expertId);
        expert.setId(ref);
        expert.setName(name);
        expert.setSkills(Set.of(skills));
        expert.setAvailabilities(List.of(avail()));
        expert.setAbsences(List.of());
        return expert;
    }

    private static Availability avail() {
        Availability a = new Availability();
        a.setYear(2026);
        a.setCalendarWeek(2);
        a.setWorkDay(1);
        a.setStartTime(LocalTime.of(9, 0));
        a.setEndTime(LocalTime.of(18, 0));
        return a;
    }

    private static Order order(long orderId, Skill required) {
        Order order = new Order();
        OrderRef ref = new OrderRef();
        ref.setId(orderId);
        order.setId(ref);
        order.setDueDate(DUE);
        order.setDiagnosisDuration(Duration.ofMinutes(60));
        order.setRequiredSkills(Set.of(required));
        order.setCustomerAvailabilities(List.of(custAvail()));
        order.setPriority(OrderPriority.LOW);
        order.setLocation(new Location(0.0, 0.0));
        return order;
    }

    private static Availability custAvail() {
        Availability a = new Availability();
        a.setYear(2026);
        a.setCalendarWeek(2);
        a.setWorkDay(1);
        a.setStartTime(LocalTime.of(9, 0));
        a.setEndTime(LocalTime.of(18, 0));
        return a;
    }
}
