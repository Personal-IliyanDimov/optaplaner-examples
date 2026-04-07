package org.imd.expertschedule.planner.cp;

import lombok.Getter;
import org.optaplanner.core.api.score.stream.uni.UniConstraintCollector;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class FairnessDetector {

    public static <A> UniConstraintCollector<A, LoadBalanceData, LoadBalanceData> loadBalance(
            Function<A, Object> groupKey,
            Function<A, Integer> valueExtractor) {

        return new UniConstraintCollector<A, LoadBalanceData, LoadBalanceData>() {

            @Override
            public Supplier<LoadBalanceData> supplier() {
                return LoadBalanceData::new;
            }

            @Override
            public BiFunction<LoadBalanceData, A, Runnable> accumulator() {
                return (resultContainer, a) -> {
                    Object mapped = groupKey.apply(a);
                    return resultContainer.apply(mapped, valueExtractor.apply(a));
                };
            }

            @Override
            public Function<LoadBalanceData, LoadBalanceData> finisher() {
                return Function.identity();
            }
        };
    }

    public static final class LoadBalanceData {

        private final Map<Object, Distribution<Integer>> groupToDistributionMap = new LinkedHashMap<>(0);

        private Runnable apply(final Object mapped, final Integer weight) {
            Distribution<Integer> rd = groupToDistributionMap.compute(mapped,
                (k, d) -> {
                    final Distribution<Integer> nd = (d == null) ? new Distribution<>() : d;
                    nd.getItems().add(weight);
                    return nd;
                });

            return () -> {
                groupToDistributionMap.compute(mapped,
                    (k, d) -> {
                        if (d != null) {
                            d.getItems().remove(weight);
                        }
                        if (d.getItems().isEmpty()) {
                            d = null;
                        }
                        return d;
                    });
            };
        }

        public BigInteger getSsdFromMean() {
            final Map<Object, Integer> groupToSumMap = new HashMap<>();
            groupToDistributionMap.entrySet().stream().forEach(entry -> {
                Object key = entry.getKey();
                Distribution<Integer> distribution = entry.getValue();
                for (Integer item : distribution.getItems()) {
                    groupToSumMap.compute(key, (k, s) -> (s == null) ? item : s + item);
                }
            });

            return calculateSumOfSquaredDeviationsFromMean(groupToSumMap.values().stream().toList());
        }

        private BigInteger calculateSumOfSquaredDeviationsFromMean(List<Integer> items) {
            if (items.isEmpty()) {
                return BigInteger.ZERO;
            }

            BigInteger squaredSum = BigInteger.ZERO;
            for (Integer item : items) {
                squaredSum = squaredSum.add(BigInteger.valueOf(item).pow(2));
            }

            final BigDecimal mean = calculateMean(items);
            final int n = items.size();

            final BigInteger nBySquaredAverage = BigDecimal.valueOf(n).multiply(mean.pow(2)).toBigInteger();
            final BigInteger ssdByMean = squaredSum.subtract(nBySquaredAverage);

            return ssdByMean;
        }

        private BigDecimal calculateMean(List<Integer> items) {
            long sum = 0L;
            for (int i = 0; i < items.size(); i++) {
                sum += items.get(i);
            }

            final BigDecimal result = BigDecimal.valueOf(sum)
                .divide(BigDecimal.valueOf(items.size()), BigDecimal.ROUND_HALF_UP);

            return result;
        }
    }

    @Getter
    private static class Distribution<T> {
        private final List<T> items ;

        public Distribution() {
            items = new ArrayList<>();
        }
    }
}
