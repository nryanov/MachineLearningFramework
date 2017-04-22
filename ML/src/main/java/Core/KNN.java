package Core;

import Data.DataNormalization;
import Data.DataSetUtilities;
import Plot.XYLineChart;
import org.ejml.simple.SimpleMatrix;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by GrIfOn on 12.03.2017.
 */
public class KNN {
    private SimpleMatrix X_train;
    private SimpleMatrix Y_train;
    private SimpleMatrix normalized_X;
    private List<DistanceAndClass> distances;
    private int neighbors;

    /**
     *
     * @param neighbors
     */
    public KNN(int neighbors) {
        if(neighbors <= 0) {
            throw new IllegalArgumentException("Amount of neighbors should be > 0");
        }

        this.neighbors = neighbors;
        distances = new ArrayList<>();
    }

    /**
     *
     * @return
     */
    public int getNeighbors() {
        return neighbors;
    }

    /**
     *
     * @param neighbors
     */
    public void setNeighbors(int neighbors) {
        this.neighbors = neighbors;
    }

    /**
     *
     * @return
     */
    public List<DistanceAndClass> getDistances() {
        return distances;
    }

    /**
     *
     * @param prediction
     * @return
     */
    private double calculateDistances(SimpleMatrix prediction) {
        for(int row = 0; row < normalized_X.numRows(); ++row) {
            distances.add(new DistanceAndClass(Math.sqrt(normalized_X.extractVector(true, row).minus(prediction).elementPower(2).elementSum()), (int)Y_train.get(row, 0)));
        }

        Collections.sort(distances);

        Map<Integer, Long> counter = distances.stream().limit(neighbors).
                collect(Collectors.groupingBy(DistanceAndClass::getClassNumber, Collectors.counting()));

        distances.clear();

        return counter.entrySet().stream().max((x, y) -> x.getValue().compareTo(y.getValue())).get().getKey();
    }

    /**
     *
     * @param x_train
     * @param y_train
     */
    public void fit(SimpleMatrix x_train, SimpleMatrix y_train) {
        Y_train = y_train;
        X_train = x_train;
        normalized_X = DataNormalization.minMaxNormalization(X_train);
    }

    /**
     *
     * @param X
     * @return
     */
    public SimpleMatrix predict(SimpleMatrix X) {
        SimpleMatrix normalized = DataNormalization.minMaxNormalization(X_train, X);
        SimpleMatrix answer = new SimpleMatrix(normalized.numRows(), normalized.numCols() + 1);

        for(int row = 0; row < normalized.numRows(); ++row) {
            answer.setRow(row, 0, X.extractVector(true, row).getMatrix().getData());
            answer.set(row, answer.numCols() - 1, calculateDistances(normalized.extractVector(true, row)));
        }

        return answer;
    }

    /**
     * Leave-one-out
     * @param X
     * @param Y
     * @return
     */
    public int LOO(SimpleMatrix X, SimpleMatrix Y, int maxK, int delta) {
        SimpleMatrix looHistory = new SimpleMatrix(maxK, 2);
        int minK = 0;
        int minError = Integer.MAX_VALUE;

        for(int k = 3; k < maxK && minError > delta; ++k) {
            this.setNeighbors(k);
            int errorCount = 0;

            for(int row = 0; row < Y.numRows(); ++row) {
                SimpleMatrix Xd = DataSetUtilities.removeRow(X, row);
                SimpleMatrix Yd = DataSetUtilities.removeRow(Y, row);

                this.fit(Xd, Yd);
                SimpleMatrix prediction = this.predict(X.extractVector(true, row));
                SimpleMatrix realAnswer = Y.extractVector(true, row);

                if(realAnswer.get(0, realAnswer.numCols() - 1) != prediction.get(0, prediction.numCols() - 1)) {
                    errorCount++;
                }
            }

            looHistory.set(k, 0, k);
            looHistory.set(k, 1, errorCount);

            if(minError > errorCount) {
                minError = errorCount;
                minK = k;
            }
        }

        plotLOOHistory(looHistory);

        return minK;
    }

    private void plotLOOHistory(SimpleMatrix looHistory) {
        XYLineChart XYLineChart = new XYLineChart("LOO", DataSetUtilities.toArray(looHistory, 0, 1));;

        XYLineChart.plot();
    }


    /**
     *
     */
    private class DistanceAndClass implements Comparable<DistanceAndClass> {
        private double distance;
        private int classNumber;

        /**
         *
         * @param distance
         * @param classNumber
         */
        private DistanceAndClass(double distance, int classNumber) {
            this.distance = distance;
            this.classNumber = classNumber;
        }

        /**
         *
         * @return
         */
        public double getDistance() {
            return distance;
        }

        /**
         *
         * @param distance
         */
        public void setDistance(double distance) {
            this.distance = distance;
        }

        /**
         *
         * @return
         */
        public int getClassNumber() {
            return classNumber;
        }

        /**
         *
         * @param classNumber
         */
        public void setClassNumber(int classNumber) {
            this.classNumber = classNumber;
        }

        /**
         *
         * @param o
         * @return
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DistanceAndClass that = (DistanceAndClass) o;

            if (Double.compare(that.distance, distance) != 0) return false;
            return classNumber == that.classNumber;

        }

        /**
         *
         * @return
         */
        @Override
        public int hashCode() {
            int result;
            long temp;
            temp = Double.doubleToLongBits(distance);
            result = (int) (temp ^ (temp >>> 32));
            result = 31 * result + classNumber;
            return result;
        }

        /**
         *
         * @return
         */
        @Override
        public String toString() {
            return "DistanceAndClass{" +
                    "distance=" + distance +
                    ", classNumber=" + classNumber +
                    '}';
        }

        /**
         *
         * @param o
         * @return
         */
        @Override
        public int compareTo(DistanceAndClass o) {
            if(this.distance < o.distance) {
                return -1;
            } else if(this.distance > o.distance) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}
