package com.example.expertschedule.planner.domain;

import com.example.expertschedule.planner.domain.refs.OrderRef;
import com.example.expertschedule.planner.domain.time.TimeSlot;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.Period;

@Getter
@Setter
public class ScheduleItem {
    private Period travelDuration;
    private OrderRef orderRef;
    private TimeSlot slot;
}

