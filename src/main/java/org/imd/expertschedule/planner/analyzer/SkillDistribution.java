package org.imd.expertschedule.planner.analyzer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class SkillDistribution {
    private LocalDate dueDate;
    private Map<String, Long> skillToMinutes;
}