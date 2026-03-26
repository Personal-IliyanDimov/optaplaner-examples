package org.imd.expertschedule.planner.cp;

import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.score.stream.uni.UniConstraintCollector;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FairnessTest {

    private record Row(String group, BigInteger weight) {}

    private static UniConstraintCollector<Row, Fairness.LoadBalanceData, Fairness.LoadBalanceData> collector() {
        return Fairness.loadBalance(Row::group, Row::weight);
    }

    @Test
    void emptyAccumulator_reportsZeroRoot() {
        UniConstraintCollector<Row, Fairness.LoadBalanceData, Fairness.LoadBalanceData> collector = collector();
        Fairness.LoadBalanceData data = collector.supplier().get();

        assertEquals(BigInteger.ZERO, data.getZeroDeviationSquaredSumRoot());
    }

    @Test
    void singleGroup_rootEqualsIntegerSqrtOfSquaredTotal() {
        UniConstraintCollector<Row, Fairness.LoadBalanceData, Fairness.LoadBalanceData> collector = collector();
        Fairness.LoadBalanceData data = collector.supplier().get();
        var acc = collector.accumulator();

        acc.apply(data, new Row("A", BigInteger.valueOf(3)));
        acc.apply(data, new Row("A", BigInteger.valueOf(4)));

        assertEquals(BigInteger.valueOf(7), data.getZeroDeviationSquaredSumRoot());
    }

    @Test
    void twoGroups_rootIsFloorSqrtOfSumOfSquaredGroupTotals() {
        UniConstraintCollector<Row, Fairness.LoadBalanceData, Fairness.LoadBalanceData> collector = collector();
        Fairness.LoadBalanceData data = collector.supplier().get();
        var acc = collector.accumulator();

        acc.apply(data, new Row("A", BigInteger.valueOf(5)));
        acc.apply(data, new Row("B", BigInteger.valueOf(5)));

        assertEquals(BigInteger.valueOf(50).sqrt(), data.getZeroDeviationSquaredSumRoot());
    }

    @Test
    void undoRestoresMetric() {
        UniConstraintCollector<Row, Fairness.LoadBalanceData, Fairness.LoadBalanceData> collector = collector();
        Fairness.LoadBalanceData data = collector.supplier().get();
        var acc = collector.accumulator();

        Runnable undo = acc.apply(data, new Row("A", BigInteger.valueOf(10)));
        assertEquals(BigInteger.TEN, data.getZeroDeviationSquaredSumRoot());

        undo.run();
        assertEquals(BigInteger.ZERO, data.getZeroDeviationSquaredSumRoot());
    }
}
