package com.example.expertschedule.io.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Root DTO for a single planning dataset file.
 * All data is read from / written to one JSON file.
 */
@Getter
@Setter
public class PlanningDatasetData {

    private List<SkillData> skills;
    private List<CustomerData> customers;
    private List<ExpertData> experts;
    private List<OrderData> orders;
}
