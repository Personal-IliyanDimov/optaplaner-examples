package org.imd.expertschedule.planner.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Location {
    private double latitude;
    private double longitude;

    public double distanceTo(Location other) {
        double dx = latitude - other.latitude;
        double dy = longitude - other.longitude;
        return Math.sqrt(dx * dx + dy * dy);
    }
}

