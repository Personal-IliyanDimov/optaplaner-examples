package org.imd.expertschedule.planner.cp;

import org.optaplanner.core.api.score.stream.uni.UniConstraintCollector;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class Fairness {

    public static <A> UniConstraintCollector<A, LoadBalanceData, LoadBalanceData> loadBalance(
            Function<A, Object> groupKey,
            Function<A, BigInteger> valueExtractor) {

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

        private final Map<Object, BigInteger> groupWeightMap = new LinkedHashMap<>(0);
        /** Sum of per-group squared totals: {@code Σ_g (weight_g)²}. */
        private BigInteger squaredSum = BigInteger.ZERO;

        private Runnable apply(Object mapped, BigInteger weight) {
            BigInteger oldWeight = groupWeightMap.getOrDefault(mapped, BigInteger.ZERO);
            BigInteger newWeight = oldWeight.add(weight);

            groupWeightMap.put(mapped, newWeight);

            // (newWeight² - oldWeight²) = 2 * oldWeight * w + w²
            BigInteger delta = oldWeight.shiftLeft(1).multiply(weight).add(weight.multiply(weight));
            squaredSum = squaredSum.add(delta);

            return () -> {
                BigInteger currentWeight = groupWeightMap.get(mapped);
                if (currentWeight == null) {
                    throw new IllegalStateException("Missing group total for undo");
                }
                BigInteger weightBeforeUndo = currentWeight.subtract(weight);

                if (weightBeforeUndo.signum() <= 0) {
                    groupWeightMap.remove(mapped);
                } else {
                    groupWeightMap.put(mapped, weightBeforeUndo);
                }

                BigInteger undoDelta = weightBeforeUndo.shiftLeft(1).multiply(weight).add(weight.multiply(weight));
                squaredSum = squaredSum.subtract(undoDelta);
            };
        }

        public BigInteger getZeroDeviationSquaredSumRoot() {
            return squaredSum.sqrt();
        }
    }
}
