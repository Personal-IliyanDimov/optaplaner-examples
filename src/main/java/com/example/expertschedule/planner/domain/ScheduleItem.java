package com.example.expertschedule.planner.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScheduleItem {
    private Order order;
    private int sequence;
}

