package org.imd.expertschedule.io.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScheduleItemData {

    private long orderId;
    private String travelDuration;
    private TimeSlotData slot;
}
