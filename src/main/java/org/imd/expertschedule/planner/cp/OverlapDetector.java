package org.imd.expertschedule.planner.cp;

import org.imd.expertschedule.planner.domain.refs.ExpertRef;
import org.imd.expertschedule.planner.util.DayInterval;
import org.optaplanner.core.api.score.stream.uni.UniConstraintCollector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class OverlapDetector {

    public static <A> UniConstraintCollector<A, OverlapData, OverlapData> buildCollector(
            Function<A, ExpertRef> groupKey,
            Function<A, DayInterval> valueExtractor) {

        return new UniConstraintCollector<A, OverlapData, OverlapData>() {

            @Override
            public Supplier<OverlapData> supplier() {
                return OverlapData::new;
            }

            @Override
            public BiFunction<OverlapData, A, Runnable> accumulator() {
                return (resultContainer, a) -> {
                    final ExpertRef expertRef = groupKey.apply(a);
                    return resultContainer.apply(expertRef, valueExtractor.apply(a));
                };
            }

            @Override
            public Function<OverlapData, OverlapData> finisher() {
                return Function.identity();
            }
        };
    }

    public static final class OverlapData {
        private final Map<ExpertRef, List<DayInterval>> expertToDayIntervalsMap = new LinkedHashMap<>(0);

        private Runnable apply(final ExpertRef expertRef, final DayInterval value) {
            List<DayInterval> expertDayIntervals = expertToDayIntervalsMap.get(expertRef);
            if (expertDayIntervals == null) {
                expertDayIntervals = new ArrayList<>();
                expertToDayIntervalsMap.put(expertRef, expertDayIntervals);
            }
            expertDayIntervals.add(value);

            return () -> {
                List<DayInterval> latestExpertDayIntervals = expertToDayIntervalsMap.get(expertRef);
                if (latestExpertDayIntervals == null) {
                    throw new IllegalStateException("Missing expert for undo. ");
                }

                latestExpertDayIntervals.remove(value);
                if (latestExpertDayIntervals.isEmpty()) {
                    expertToDayIntervalsMap.remove(expertRef);
                }
            };
        }

        public List<DayInterval> extractExpertDayIntervals(ExpertRef expertRef) {
            return expertToDayIntervalsMap.get(expertRef);
        }
    }
}
