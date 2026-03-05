package com.example.expertschedule.io.generator;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Configuration for generating a planning dataset.
 * Use presets from {@link GeneratorConfigPresets} or build custom with {@link #builder()}.
 */
@Getter
@Setter
@Builder
public class GeneratorConfig {

    private int numSkills;
    private int numExperts;
    private int numCustomers;
    private int numOrders;

    /** Number of back offices (experts are assigned to one office each). */
    @Builder.Default
    private int numOffices = 1;

    /** Number of experts that will have at least one availability entry. */
    @Builder.Default
    private int expertsWithAvailability = 0;

    /** Number of experts that will have at least one absence entry. */
    @Builder.Default
    private int expertsWithAbsence = 0;
}
