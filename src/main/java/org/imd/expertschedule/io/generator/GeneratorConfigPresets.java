package org.imd.expertschedule.io.generator;

/**
 * Preset configurations for different dataset sizes.
 * Use with {@link TestDataGenerator#generate(GeneratorConfig, java.nio.file.Path)}.
 */
public final class GeneratorConfigPresets {

    private GeneratorConfigPresets() {
    }

    public static GeneratorConfig small() {
        return GeneratorConfig.builder()
                .numSkills(15)
                .numExperts(5)
                .numCustomers(10)
                .numOrders(20)
                .numOffices(1)
                .expertsWithAvailability(5)
                .expertsWithAbsence(1)
                .orderPriorities(new String[] {"LOW", "MEDIUM", "HIGH"})
                .orderDurations(new String[] { "PT30M", "PT1H", "PT1H30M", "PT2H" })
                .build();
    }

    public static GeneratorConfig medium() {
        return GeneratorConfig.builder()
                .numSkills(15)
                .numExperts(15)
                .numCustomers(20)
                .numOrders(50)
                .numOffices(3)
                .expertsWithAvailability(15)
                .expertsWithAbsence(3)
                .orderPriorities(new String[] {"LOW", "MEDIUM", "HIGH"})
                .orderDurations(new String[] { "PT30M", "PT1H", "PT1H30M", "PT2H" })
                .build();
    }

    public static GeneratorConfig large() {
        return GeneratorConfig.builder()
                .numSkills(15)
                .numExperts(30)
                .numCustomers(100)
                .numOrders(100)
                .numOffices(6)
                .expertsWithAvailability(30)
                .expertsWithAbsence(5)
                .orderPriorities(new String[] {"LOW", "MEDIUM", "HIGH"})
                .orderDurations(new String[] { "PT30M", "PT1H", "PT1H30M", "PT2H" })
                .build();
    }

    public static GeneratorConfig extraLarge() {
        return GeneratorConfig.builder()
                .numSkills(15)
                .numExperts(50)
                .numCustomers(1000)
                .numOrders(100)
                .numOffices(10)
                .expertsWithAvailability(50)
                .expertsWithAbsence(10)
                .orderPriorities(new String[] {"LOW", "MEDIUM", "HIGH"})
                .orderDurations(new String[] { "PT30M", "PT1H", "PT1H30M", "PT2H" })
                .build();
    }
}
