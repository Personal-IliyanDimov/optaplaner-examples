package com.example.expertschedule.io.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class OrderData {

    private long id;
    /** Optional display code (e.g. ORDER-1). */
    private String code;
    private long customerId;
    private LocationData location;
    private List<String> requiredSkills;
    private LocalDate dueDate;
    /** LOW, MEDIUM, or HIGH. */
    private String priority;
}
