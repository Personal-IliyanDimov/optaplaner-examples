package org.imd.expertschedule.planner.solution;

import org.imd.expertschedule.planner.domain.Expert;
import org.imd.expertschedule.planner.domain.ExpertSchedule;
import org.imd.expertschedule.planner.domain.Location;
import org.imd.expertschedule.planner.domain.Order;
import org.imd.expertschedule.planner.domain.ScheduleItem;
import org.imd.expertschedule.planner.domain.Skill;
import org.imd.expertschedule.planner.domain.time.Absence;
import org.imd.expertschedule.planner.domain.time.Availability;
import org.imd.expertschedule.planner.domain.time.WeekPeriod;
import org.imd.expertschedule.planner.util.PlannerHelper;

import java.io.PrintStream;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

public final class ExpertPlanningSolutionPrinter {

    public void print(ExpertPlanningSolution solution) {
        print(solution, System.out);
    }

    public void print(ExpertPlanningSolution solution, PrintStream out) {
        out.println("Best score: " + solution.getScore());
        out.println();

        PlannerHelper helper = solution.getHelper();
        TreeMap<LocalDate, List<ExpertSchedule>> byDate = new TreeMap<>();
        for (ExpertSchedule schedule : solution.getExpertScheduleList()) {
            byDate.computeIfAbsent(schedule.getDate(), d -> new ArrayList<>()).add(schedule);
        }
        for (List<ExpertSchedule> daySchedules : byDate.values()) {
            daySchedules.sort(Comparator.comparing(s -> Optional.ofNullable(s.getExpert().getName()).orElse("")));
        }

        List<ScheduleItem> items = solution.getScheduleItemList() != null
                ? solution.getScheduleItemList()
                : List.of();

        for (Map.Entry<LocalDate, List<ExpertSchedule>> entry : byDate.entrySet()) {
            LocalDate date = entry.getKey();
            out.println("========== " + date + " (" + date.getDayOfWeek() + ") ==========");
            for (ExpertSchedule schedule : entry.getValue()) {
                printExpertWorkingDay(out, helper, schedule, items);
            }
            out.println();
        }
    }

    private void printExpertWorkingDay(
            PrintStream out, PlannerHelper helper, ExpertSchedule schedule, List<ScheduleItem> items) {
        Expert expert = schedule.getExpert();
        LocalDate date = schedule.getDate();

        out.println("--- Expert: " + expertLabel(expert) + " ---");
        out.println("Expert skills: " + formatSkills(expert.getSkills()));

        List<Availability> availabilities = filterAvailabilitiesForDate(expert.getAvailabilities(), date, helper);
        out.println("Expert availability: " + formatWeekPeriods(availabilities));

        List<Absence> absences = filterAbsencesForDate(expert.getAbsences(), date, helper);
        out.println("Expert absence: " + formatAbsences(absences));

        out.println("Expert scheduled items:");
        List<ScheduleItem> assigned = items.stream()
                .filter(i -> i.getExpertSchedule() == schedule && i.getTimeSlot() != null)
                .sorted(Comparator.comparing(i -> i.getTimeSlot().getStartTime()))
                .toList();
        if (assigned.isEmpty()) {
            out.println("  (none)");
        } else {
            for (ScheduleItem item : assigned) {
                printScheduledItemLine(out, item);
            }
        }
        out.println();
    }

    private void printScheduledItemLine(PrintStream out, ScheduleItem item) {
        Order order = item.getOrder();
        String customerName = "n/a";
        String dueDateStr = "n/a";
        String durationStr = "n/a";
        String locationStr = "n/a";
        if (order != null) {
            if (order.getCustomer() != null && order.getCustomer().getName() != null) {
                customerName = order.getCustomer().getName();
            }
            if (order.getDueDate() != null) {
                dueDateStr = order.getDueDate().toString();
            }
            durationStr = formatDuration(order.getDiagnosisDuration());
            locationStr = formatLocation(order.getLocation());
        }
        LocalTime start = item.getTimeSlot().getStartTime();
        out.printf("  - customer: %s | due: %s | start: %s | duration: %s | location: %s%n",
                customerName, dueDateStr, start, durationStr, locationStr);
    }

    private String expertLabel(Expert expert) {
        if (expert.getName() != null && !expert.getName().isBlank()) {
            return expert.getName();
        }
        if (expert.getId() != null) {
            return "Expert #" + expert.getId().getId();
        }
        return "Expert (unnamed)";
    }

    private String formatSkills(Set<Skill> skills) {
        if (skills == null || skills.isEmpty()) {
            return "(none)";
        }
        return skills.stream()
                .map(Skill::getName)
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.joining(", "));
    }

    private List<Availability> filterAvailabilitiesForDate(
            List<Availability> list, LocalDate date, PlannerHelper helper) {
        if (list == null || list.isEmpty()) {
            return List.of();
        }
        return list.stream()
                .filter(a -> a.getDayOfWeek() != null
                        && date.equals(helper.calculateDate(a.getCalendarWeek(), a.getDayOfWeek().getValue())))
                .toList();
    }

    private List<Absence> filterAbsencesForDate(List<Absence> list, LocalDate date, PlannerHelper helper) {
        if (list == null || list.isEmpty()) {
            return List.of();
        }
        return list.stream()
                .filter(a -> a.getDayOfWeek() != null
                        && date.equals(helper.calculateDate(a.getCalendarWeek(), a.getDayOfWeek().getValue())))
                .toList();
    }

    private String formatWeekPeriods(List<? extends WeekPeriod> periods) {
        if (periods.isEmpty()) {
            return "(none for this day)";
        }
        return periods.stream()
                .map(this::formatWeekPeriodLine)
                .collect(Collectors.joining("; "));
    }

    private String formatWeekPeriodLine(WeekPeriod p) {
        return String.format("%s–%s (calendar week %d)",
                p.getStartTime(), p.getEndTime(), p.getCalendarWeek());
    }

    private String formatAbsences(List<Absence> absences) {
        if (absences.isEmpty()) {
            return "(none for this day)";
        }
        return absences.stream()
                .map(a -> {
                    String base = formatWeekPeriodLine(a);
                    if (a.getReason() != null && !a.getReason().isBlank()) {
                        return base + " — " + a.getReason();
                    }
                    return base;
                })
                .collect(Collectors.joining("; "));
    }

    private String formatDuration(Duration duration) {
        if (duration == null) {
            return "n/a";
        }
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        if (hours > 0 && minutes > 0) {
            return hours + "h " + minutes + "m";
        }
        if (hours > 0) {
            return hours + "h";
        }
        if (minutes > 0) {
            return minutes + "m";
        }
        return duration.toString();
    }

    private String formatLocation(Location location) {
        if (location == null) {
            return "n/a";
        }
        return String.format("(%.5f, %.5f)", location.getLatitude(), location.getLongitude());
    }
}
