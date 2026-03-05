package com.example.expertschedule.io.generator;

/**
 * Preset configurations for different dataset sizes.
 * Use with {@link TestDataGenerator#generate(GeneratorConfig, java.nio.file.Path)}.
 */
public final class GeneratorConfigPresets {

    private GeneratorConfigPresets() {
    }

    public static GeneratorConfig small() {
        return GeneratorConfig.builder()
                .numSkills(5)
                .numExperts(5)
                .numCustomers(10)
                .numOrders(20)
                .expertsWithAvailability(5)
                .expertsWithAbsence(1)
                .build();
    }

    public static GeneratorConfig medium() {
        return GeneratorConfig.builder()
                .numSkills(5)
                .numExperts(15)
                .numCustomers(20)
                .numOrders(50)
                .expertsWithAvailability(15)
                .expertsWithAbsence(3)
                .build();
    }

    public static GeneratorConfig large() {
        return GeneratorConfig.builder()
                .numSkills(10)
                .numExperts(30)
                .numCustomers(100)
                .numOrders(100)
                .expertsWithAvailability(30)
                .expertsWithAbsence(5)
                .build();
    }

    public static GeneratorConfig extraLarge() {
        return GeneratorConfig.builder()
                .numSkills(10)
                .numExperts(50)
                .numCustomers(1000)
                .numOrders(100)
                .expertsWithAvailability(50)
                .expertsWithAbsence(10)
                .build();
    }

}
