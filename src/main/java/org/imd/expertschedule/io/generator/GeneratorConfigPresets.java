package org.imd.expertschedule.io.generator;

import java.time.Duration;

/**
 * Preset configurations for different dataset sizes.
 * Use with {@link TestDataGenerator#generate(GeneratorConfig, java.nio.file.Path)}.
 */
public final class GeneratorConfigPresets {

    private static final int CUSTOMER_AVAILABILITY_TW = 240;
    private static final int PLANNING_YEAR = 2026;
    private static final int CALENDAR_WEEK = 10;
    private static final int[] WEEK_WORKING_DAYS = new int[] {1, 2, 3, 4, 5};

    private GeneratorConfigPresets() {
    }

    public static GeneratorConfig ultrasmall() {
        GeneratorConfig config = new GeneratorConfig();
        config.setFileName("dataset-ultrasmall.json");
        config.setNumSkills(5);
        config.setNumCustomers(10);
        config.setNumOffices(1);
        config.setExpertsPerBackOffice(5);
        config.setOrdersPerExpert(5);
        config.setExpertsPerOfficeWithUndefaultAvailability(1);
        config.setExpertsPerOfficeWithAbsence(1);
        config.setOrderPriorities(new String[] {"LOW", "MEDIUM", "HIGH"});
        config.setOrderDurations(new Duration[] {Duration.parse("PT30M"), Duration.parse("PT1H"),
                                                 Duration.parse("PT1H30M"), Duration.parse("PT2H")});
        config.setCustomerAvailabilityTimeWindowInMinutes(CUSTOMER_AVAILABILITY_TW);
        config.setYear(PLANNING_YEAR);
        config.setCalendarWeek(CALENDAR_WEEK);
        config.setWeekWorkingDays(WEEK_WORKING_DAYS);
        config.setMaxDistanceFromBackOfficeKm(20);
        return config;
    }

    public static GeneratorConfig small() {
        GeneratorConfig config = new GeneratorConfig();
        config.setFileName("dataset-small.json");
        config.setNumSkills(15);
        config.setNumCustomers(10);
        config.setNumOffices(2);
        config.setExpertsPerBackOffice(5);
        config.setOrdersPerExpert(5);
        config.setExpertsPerOfficeWithUndefaultAvailability(2);
        config.setExpertsPerOfficeWithAbsence(1);
        config.setOrderPriorities(new String[] {"LOW", "MEDIUM", "HIGH"});
        config.setOrderDurations(new Duration[] {Duration.parse("PT30M"), Duration.parse("PT1H"),
                Duration.parse("PT1H30M")});
        config.setCustomerAvailabilityTimeWindowInMinutes(CUSTOMER_AVAILABILITY_TW);
        config.setYear(PLANNING_YEAR);
        config.setCalendarWeek(CALENDAR_WEEK);
        config.setWeekWorkingDays(WEEK_WORKING_DAYS);
        config.setMaxDistanceFromBackOfficeKm(20);
        return config;
    }

    public static GeneratorConfig medium() {
        GeneratorConfig config = new GeneratorConfig();
        config.setFileName("dataset-medium.json");
        config.setNumSkills(15);
        config.setNumCustomers(20);
        config.setNumOffices(3);
        config.setExpertsPerBackOffice(10);
        config.setOrdersPerExpert(10);
        config.setExpertsPerOfficeWithUndefaultAvailability(2);
        config.setExpertsPerOfficeWithAbsence(1);
        config.setOrderPriorities(new String[] {"LOW", "MEDIUM", "HIGH"});
        config.setOrderDurations(new Duration[] {Duration.parse("PT30M"), Duration.parse("PT1H"),
                Duration.parse("PT1H30M")});
        config.setCustomerAvailabilityTimeWindowInMinutes(CUSTOMER_AVAILABILITY_TW);
        config.setYear(PLANNING_YEAR);
        config.setCalendarWeek(CALENDAR_WEEK);
        config.setWeekWorkingDays(WEEK_WORKING_DAYS);
        config.setMaxDistanceFromBackOfficeKm(30);
        return config;
    }

    public static GeneratorConfig large() {
        GeneratorConfig config = new GeneratorConfig();
        config.setFileName("dataset-large.json");
        config.setNumSkills(15);
        config.setNumCustomers(100);
        config.setNumOffices(3);
        config.setExpertsPerBackOffice(20);
        config.setOrdersPerExpert(10);
        config.setExpertsPerOfficeWithUndefaultAvailability(4);
        config.setExpertsPerOfficeWithAbsence(2);
        config.setOrderPriorities(new String[] {"LOW", "MEDIUM", "HIGH"});
        config.setOrderDurations(new Duration[] {Duration.parse("PT30M"), Duration.parse("PT1H"),
                Duration.parse("PT1H30M")});
        config.setCustomerAvailabilityTimeWindowInMinutes(CUSTOMER_AVAILABILITY_TW);
        config.setYear(PLANNING_YEAR);
        config.setCalendarWeek(CALENDAR_WEEK);
        config.setWeekWorkingDays(WEEK_WORKING_DAYS);
        config.setMaxDistanceFromBackOfficeKm(30);
        return config;
    }

    public static GeneratorConfig extraLarge() {
        GeneratorConfig config = new GeneratorConfig();
        config.setFileName("dataset-extralarge.json");
        config.setNumSkills(15);
        config.setNumCustomers(100);
        config.setNumOffices(5);
        config.setExpertsPerBackOffice(50);
        config.setOrdersPerExpert(16);
        config.setExpertsPerOfficeWithUndefaultAvailability(20);
        config.setExpertsPerOfficeWithAbsence(10);
        config.setOrderPriorities(new String[] {"LOW", "MEDIUM", "HIGH"});
        config.setOrderDurations(new Duration[] {Duration.parse("PT30M"), Duration.parse("PT45M"),
                Duration.parse("PT1H"), Duration.parse("PT1H15M")});
        config.setCustomerAvailabilityTimeWindowInMinutes(CUSTOMER_AVAILABILITY_TW);
        config.setYear(PLANNING_YEAR);
        config.setCalendarWeek(CALENDAR_WEEK);
        config.setWeekWorkingDays(WEEK_WORKING_DAYS);
        config.setMaxDistanceFromBackOfficeKm(20);
        return config;
    }

    public static GeneratorConfig huge() {
        GeneratorConfig config = new GeneratorConfig();
        config.setFileName("dataset-huge.json");
        config.setNumSkills(15);
        config.setNumCustomers(100);
        config.setNumOffices(10);
        config.setExpertsPerBackOffice(100);
        config.setOrdersPerExpert(11);
        config.setExpertsPerOfficeWithUndefaultAvailability(20);
        config.setExpertsPerOfficeWithAbsence(10);
        config.setOrderPriorities(new String[] {"LOW", "MEDIUM", "HIGH"});
        config.setOrderDurations(new Duration[] {Duration.parse("PT30M"), Duration.parse("PT45M"),
                Duration.parse("PT1H"), Duration.parse("PT1H15M"), Duration.parse("PT1H30M")});
        config.setCustomerAvailabilityTimeWindowInMinutes(CUSTOMER_AVAILABILITY_TW);
        config.setYear(PLANNING_YEAR);
        config.setCalendarWeek(CALENDAR_WEEK);
        config.setWeekWorkingDays(WEEK_WORKING_DAYS);
        config.setMaxDistanceFromBackOfficeKm(20);
        return config;
    }
}
