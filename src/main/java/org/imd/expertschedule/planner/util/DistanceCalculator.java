package org.imd.expertschedule.planner.util;

import org.imd.expertschedule.planner.domain.Location;

public class DistanceCalculator {
    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double DEG_TO_RAD = Math.PI / 180.0;

    public double calculateDistance(final Location start, final Location end) {
        // Extract values once (avoid repeated getter calls)
        final double lat1 = start.getLatitude();
        final double lon1 = start.getLongitude();
        final double lat2 = end.getLatitude();
        final double lon2 = end.getLongitude();

        // Convert once using multiplication (faster than Math.toRadians)
        final double lat1Rad = lat1 * DEG_TO_RAD;
        final double lat2Rad = lat2 * DEG_TO_RAD;
        final double dLat = (lat2 - lat1) * DEG_TO_RAD;
        final double dLon = (lon2 - lon1) * DEG_TO_RAD;

        final double sinLat = Math.sin(dLat * 0.5);
        final double sinLon = Math.sin(dLon * 0.5);

        final double a = sinLat * sinLat
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * sinLon * sinLon;

        return EARTH_RADIUS_KM * (2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a)));
    }
}
