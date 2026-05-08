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

        final double sinHalfLat = Math.sin(dLat * 0.5);
        final double sinHalfLon = Math.sin(dLon * 0.5);

        final double a = sinHalfLat * sinHalfLat
                + Math.cos(lat1Rad) * Math.cos(lat2Rad) * sinHalfLon * sinHalfLon;

        // 2 * asin(sqrt(a)) == 2 * atan2(sqrt(a), sqrt(1-a)); one sqrt vs two, same numerics with clamp
        return 2.0 * EARTH_RADIUS_KM * Math.asin(Math.min(1.0, Math.sqrt(a)));
    }
}
