package com.example.expertschedule.planner.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class ExpertSchedule {

    private Expert expert;
    private LocalDate date;
    private List<ScheduleItem> items;
}

