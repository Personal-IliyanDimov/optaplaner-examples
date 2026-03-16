package org.imd.expertschedule.io.generator;

/**
 * Preset configurations for different dataset sizes.
 * Use with {@link TestDataGenerator#generate(GeneratorConfig, java.nio.file.Path)}.
 */
public final class GeneratorConfigPresets {

    private static final int WEEK_DAYS = 5;
    private static final int AVERAGE_ORDERS_PER_DAY = 4;

    private static final int CALENDAR_WEEK = 10;
    private static final int[] WEEK_WORKING_DAYS = new int[] {1,2,3,4,5};

    private GeneratorConfigPresets() {
    }

    public static GeneratorConfig small() {
        final int numExperts = 5;
        return GeneratorConfig.builder()
                .fileName("dataset-small.json")
                .numSkills(15)
                .numExperts(numExperts)
                .numCustomers(10)
                .numOrders(numExperts*WEEK_DAYS*AVERAGE_ORDERS_PER_DAY)
                .numOffices(1)
                .expertsWithAvailability(5)
                .expertsWithAbsence(1)
                .orderPriorities(new String[] {"LOW", "MEDIUM", "HIGH"})
                .orderDurations(new String[] { "PT30M", "PT1H", "PT1H30M", "PT2H" })
                .calendarWeek(CALENDAR_WEEK)
                .weekWorkingDays(WEEK_WORKING_DAYS)
                .build();
    }

    public static GeneratorConfig medium() {
        final int numExperts = 15;
        return GeneratorConfig.builder()
                .fileName("dataset-medium.json")
                .numSkills(15)
                .numExperts(numExperts)
                .numCustomers(20)
                .numOrders(numExperts*WEEK_DAYS*AVERAGE_ORDERS_PER_DAY)
                .numOffices(3)
                .expertsWithAvailability(15)
                .expertsWithAbsence(3)
                .orderPriorities(new String[] {"LOW", "MEDIUM", "HIGH"})
                .orderDurations(new String[] { "PT30M", "PT1H", "PT1H30M", "PT2H" })
                .calendarWeek(CALENDAR_WEEK)
                .weekWorkingDays(WEEK_WORKING_DAYS)
                .build();
    }

    public static GeneratorConfig large() {
        final int numExperts = 30;
        return GeneratorConfig.builder()
                .fileName("dataset-large.json")
                .numSkills(15)
                .numExperts(numExperts)
                .numCustomers(100)
                .numOrders(numExperts*WEEK_DAYS*AVERAGE_ORDERS_PER_DAY)
                .numOffices(6)
                .expertsWithAvailability(30)
                .expertsWithAbsence(5)
                .orderPriorities(new String[] {"LOW", "MEDIUM", "HIGH"})
                .orderDurations(new String[] { "PT30M", "PT1H", "PT1H30M", "PT2H" })
                .calendarWeek(CALENDAR_WEEK)
                .weekWorkingDays(WEEK_WORKING_DAYS)
                .build();
    }

    public static GeneratorConfig extraLarge() {
        final int numExperts = 50;
        return GeneratorConfig.builder()
                .fileName("dataset-extra-large.json")
                .numSkills(15)
                .numExperts(numExperts)
                .numCustomers(1000)
                .numOrders(numExperts*WEEK_DAYS*AVERAGE_ORDERS_PER_DAY)
                .numOffices(10)
                .expertsWithAvailability(50)
                .expertsWithAbsence(10)
                .orderPriorities(new String[] {"LOW", "MEDIUM", "HIGH"})
                .orderDurations(new String[] { "PT30M", "PT1H", "PT1H30M", "PT2H" })
                .calendarWeek(CALENDAR_WEEK)
                .weekWorkingDays(WEEK_WORKING_DAYS)
                .build();
    }
}
