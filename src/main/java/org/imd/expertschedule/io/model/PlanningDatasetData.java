package org.imd.expertschedule.io.model;

import lombok.Getter;
import lombok.Setter;
import org.imd.expertschedule.io.generator.GeneratorConfig;

import java.util.List;

/**
 * Root DTO for a single planning dataset file.
 * All data is read from / written to one JSON file.
 */
@Getter
@Setter
public class PlanningDatasetData {
    private List<SkillData> skills;
    private List<BackOfficeData> backOffices;
    private List<CustomerData> customers;
    private List<ExpertData> experts;
    private List<OrderData> orders;
    private List<ExpertScheduleData> expertSchedules;
}
