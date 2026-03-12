package org.imd.expertschedule.io.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class ExpertScheduleData {

    private long expertId;
    private LocalDate date;
    private List<ScheduleItemData> items;
}
