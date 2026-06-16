package org.imd.expertschedule.io.generator;

import org.imd.expertschedule.io.model.LocationData;
import org.junit.jupiter.api.RepeatedTest;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TestDataGeneratorGeoTest {

    @RepeatedTest(50)
    void randomLocationNear_samplesWithinMaxRadiusKm() {
        Random random = new Random();
        double centerLat = 52.0 + random.nextDouble();
        double centerLon = 13.0 + random.nextDouble();
        double maxKm = 100.0;

        LocationData p = TestDataGeneratorBackUp.randomLocationNear(centerLat, centerLon, maxKm, random);
        double d = TestDataGeneratorBackUp.haversineKm(centerLat, centerLon, p.getLatitude(), p.getLongitude());

        assertTrue(d <= maxKm + 1e-6, "distance " + d + " should be <= " + maxKm);
    }

    @RepeatedTest(10)
    void haversineKm_matchesApproximateShortChord() {
        double d = TestDataGeneratorBackUp.haversineKm(52.0, 13.0, 52.1, 13.0);
        assertTrue(d > 10 && d < 15);
    }
}
