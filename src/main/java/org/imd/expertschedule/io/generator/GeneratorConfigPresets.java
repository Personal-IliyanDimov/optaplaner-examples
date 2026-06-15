package org.imd.expertschedule.io.generator;

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
        config.setExpertsWithUndefaultAvailability(1);
        config.setExpertsWithAbsence(1);
        config.setOrderPriorities(new String[] {"LOW", "MEDIUM", "HIGH"});
        config.setOrderDurations(new String[] {"PT30M", "PT1H", "PT1H30M", "PT2H"});
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
        config.setExpertsWithUndefaultAvailability(2);
        config.setExpertsWithAbsence(1);
        config.setOrderPriorities(new String[] {"LOW", "MEDIUM", "HIGH"});
        config.setOrderDurations(new String[] {"PT30M", "PT1H", "PT1H30M"});
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
        config.setExpertsWithUndefaultAvailability(2);
        config.setExpertsWithAbsence(1);
        config.setOrderPriorities(new String[] {"LOW", "MEDIUM", "HIGH"});
        config.setOrderDurations(new String[] {"PT30M", "PT1H", "PT1H30M"});
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
        config.setExpertsWithUndefaultAvailability(2);
        config.setExpertsWithAbsence(1);
        config.setOrderPriorities(new String[] {"LOW", "MEDIUM", "HIGH"});
        config.setOrderDurations(new String[] {"PT30M", "PT1H", "PT1H30M"});
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
        config.setExpertsWithUndefaultAvailability(2);
        config.setExpertsWithAbsence(1);
        config.setOrderPriorities(new String[] {"LOW", "MEDIUM", "HIGH"});
        config.setOrderDurations(new String[] {"PT30M", "PT45M", "PT1H", "PT1H15M"});
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
        config.setExpertsPerBackOffice(20);
        config.setOrdersPerExpert(11);
        config.setExpertsWithUndefaultAvailability(3);
        config.setExpertsWithAbsence(2);
        config.setOrderPriorities(new String[] {"LOW", "MEDIUM", "HIGH"});
        config.setOrderDurations(new String[] {"PT30M", "PT45M", "PT1H", "PT1H15M", "PT1H30M"});
        config.setCustomerAvailabilityTimeWindowInMinutes(CUSTOMER_AVAILABILITY_TW);
        config.setYear(PLANNING_YEAR);
        config.setCalendarWeek(CALENDAR_WEEK);
        config.setWeekWorkingDays(WEEK_WORKING_DAYS);
        config.setMaxDistanceFromBackOfficeKm(20);
        return config;
    }
}
