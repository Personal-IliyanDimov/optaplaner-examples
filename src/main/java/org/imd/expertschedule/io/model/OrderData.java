package org.imd.expertschedule.io.model;

import lombok.Getter;
import lombok.Setter;

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
    /** ISO-8601 duration (e.g. PT30M, PT1H, PT2H) - between 30m and 2h. */
    private String diagnosisDuration;
}
