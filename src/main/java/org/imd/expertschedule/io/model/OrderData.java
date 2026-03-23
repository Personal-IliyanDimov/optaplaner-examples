package org.imd.expertschedule.io.model;

import lombok.Getter;
import lombok.Setter;
import org.imd.expertschedule.planner.domain.time.Availability;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class OrderData {

    private long id;
    private String code;
    private long customerId;
    private LocationData location;
    private List<String> requiredSkills;
    private LocalDate dueDate;
    private String priority;
    private String diagnosisDuration;
    private List<Availability> customerAvailabilities;
}
