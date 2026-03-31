package org.imd.expertschedule.io.generator;

/**
 * Preset configurations for different dataset sizes.
 * Use with {@link TestDataGenerator#generate(GeneratorConfig, java.nio.file.Path)}.
 */
public final class GeneratorConfigPresets {

    private static final int WEEK_DAYS = 5;
    private static final int AVERAGE_ORDERS_PER_DAY = 4;

    private static final int PLANNING_YEAR = 2026;
    private static final int CALENDAR_WEEK = 10;
    private static final int[] WEEK_WORKING_DAYS = new int[] {1,2,3,4,5};

    private GeneratorConfigPresets() {
    }

    public static GeneratorConfig ultrasmall() {
        final int numExperts = 3;

        GeneratorConfig config = new GeneratorConfig();
        config.setFileName("dataset-ultrasmall.json");
        config.setNumSkills(5);
        config.setNumExperts(numExperts);
        config.setNumCustomers(10);
        config.setNumOrders(20);
        config.setNumOffices(1);
        config.setExpertsWithUndefaultAvailability(1);
        config.setExpertsWithAbsence(1);
        config.setOrderPriorities(new String[] {"LOW", "MEDIUM", "HIGH"});
        config.setOrderDurations(new String[] { "PT30M", "PT1H", "PT1H30M", "PT2H" });
        config.setCustomerAvailabilityTimeWindowInMinutes(240);
        config.setYear(PLANNING_YEAR);
        config.setCalendarWeek(CALENDAR_WEEK);
        config.setWeekWorkingDays(WEEK_WORKING_DAYS);

        return config;
    }

    public static GeneratorConfig small() {
        final int numExperts = 5;

        GeneratorConfig config = new GeneratorConfig();
        config.setFileName("dataset-small.json");
        config.setNumSkills(15);
        config.setNumExperts(numExperts);
        config.setNumCustomers(10);
        config.setNumOrders(numExperts*WEEK_DAYS*AVERAGE_ORDERS_PER_DAY);
        config.setNumOffices(1);
        config.setExpertsWithUndefaultAvailability(2);
        config.setExpertsWithAbsence(1);
        config.setOrderPriorities(new String[] {"LOW", "MEDIUM", "HIGH"});
        config.setOrderDurations(new String[] { "PT30M", "PT1H", "PT1H30M", "PT2H" });
        config.setCustomerAvailabilityTimeWindowInMinutes(180);
        config.setYear(PLANNING_YEAR);
        config.setCalendarWeek(CALENDAR_WEEK);
        config.setWeekWorkingDays(WEEK_WORKING_DAYS);

        return config;
    }

    public static GeneratorConfig medium() {
        final int numExperts = 15;

        GeneratorConfig config = new GeneratorConfig();
        config.setFileName("dataset-medium.json");
        config.setNumSkills(15);
        config.setNumExperts(numExperts);
        config.setNumCustomers(20);
        config.setNumOrders(numExperts*WEEK_DAYS*AVERAGE_ORDERS_PER_DAY);
        config.setNumOffices(3);
        config.setExpertsWithUndefaultAvailability(5);
        config.setExpertsWithAbsence(3);
        config.setOrderPriorities(new String[] {"LOW", "MEDIUM", "HIGH"});
        config.setOrderDurations(new String[] { "PT30M", "PT1H", "PT1H30M", "PT2H" });
        config.setCustomerAvailabilityTimeWindowInMinutes(180);
        config.setYear(PLANNING_YEAR);
        config.setCalendarWeek(CALENDAR_WEEK);
        config.setWeekWorkingDays(WEEK_WORKING_DAYS);

        return config;
    }

    public static GeneratorConfig large() {
        final int numExperts = 30;

        GeneratorConfig config = new GeneratorConfig();
        config.setFileName("dataset-large.json");
        config.setNumSkills(15);
        config.setNumExperts(numExperts);
        config.setNumCustomers(100);
        config.setNumOrders(numExperts*WEEK_DAYS*AVERAGE_ORDERS_PER_DAY);
        config.setNumOffices(6);
        config.setExpertsWithUndefaultAvailability(10);
        config.setExpertsWithAbsence(5);
        config.setOrderPriorities(new String[] {"LOW", "MEDIUM", "HIGH"});
        config.setOrderDurations(new String[] { "PT30M", "PT1H", "PT1H30M", "PT2H" });
        config.setCustomerAvailabilityTimeWindowInMinutes(180);
        config.setYear(PLANNING_YEAR);
        config.setCalendarWeek(CALENDAR_WEEK);
        config.setWeekWorkingDays(WEEK_WORKING_DAYS);

        return config;
    }

    public static GeneratorConfig extraLarge() {
        final int numExperts = 50;

        GeneratorConfig config = new GeneratorConfig();
        config.setFileName("dataset-extralarge.json");
        config.setNumSkills(15);
        config.setNumExperts(numExperts);
        config.setNumCustomers(1000);
        config.setNumOrders(numExperts*WEEK_DAYS*AVERAGE_ORDERS_PER_DAY);
        config.setNumOffices(10);
        config.setExpertsWithUndefaultAvailability(10);
        config.setExpertsWithAbsence(10);
        config.setOrderPriorities(new String[] {"LOW", "MEDIUM", "HIGH"});
        config.setOrderDurations(new String[] { "PT30M", "PT1H", "PT1H30M", "PT2H" });
        config.setCustomerAvailabilityTimeWindowInMinutes(180);
        config.setYear(PLANNING_YEAR);
        config.setCalendarWeek(CALENDAR_WEEK);
        config.setWeekWorkingDays(WEEK_WORKING_DAYS);

        return config;
    }
}
