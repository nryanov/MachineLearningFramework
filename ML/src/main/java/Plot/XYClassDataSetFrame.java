package Plot;

import Data.DataSet;
import Data.DataSetUtilities;
import Plot.Interfaces.Plotter;
import org.apache.commons.collections.map.MultiValueMap;
import org.ejml.simple.SimpleMatrix;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import javax.swing.*;
import java.util.List;
import java.util.Map;

/**
 * Created by GrIfOn on 17.03.2017.
 */
public abstract class XYClassDataSetFrame extends ApplicationFrame implements Plotter {
    protected XYSeriesCollection xyDataset;

    public XYClassDataSetFrame(String title) {
        super(title);
    }

    public XYSeriesCollection getXyDataset() {
        return xyDataset;
    }

    /**
     *
     * @param dataSet
     */
    protected void setXyDataset(DataSet dataSet) {
        xyDataset = new XYSeriesCollection();

        Map<String, SimpleMatrix> classMap = new MultiValueMap();
        for(int i = 0; i < dataSet.getMatrix().numRows(); ++i) {
            classMap.put(String.valueOf(dataSet.getMatrix().get(i, dataSet.getMatrix().numCols() - 1)),
                    dataSet.getMatrix().extractVector(true, i));
        }

        for(Map.Entry<String, SimpleMatrix> entry : classMap.entrySet()) {

            List<SimpleMatrix> values = (List<SimpleMatrix>) entry.getValue();
            XYSeries xySeries = new XYSeries(entry.getKey());

            values.stream().forEach((x) -> xySeries.add(x.get(0, 0), x.get(0, 1)));

            xyDataset.addSeries(xySeries);
        }
    }

    /**
     *
     * @param dataSet
     * @param x1Column
     * @param x2Column
     * @param yColumn
     */
    protected void setXyDataset(DataSet dataSet, int x1Column, int x2Column, int yColumn) {
        SimpleMatrix X = dataSet.getTrainingSet(x1Column, x2Column);
        SimpleMatrix Y = dataSet.getAnswersSet(yColumn);

        setXyDataset(DataSetUtilities.addColumns(X, Y));
    }

    /**
     *
     * @param dataSet
     */
    protected void setXyDataset(double[][] dataSet) {
        SimpleMatrix matrix = new SimpleMatrix(dataSet);
        setXyDataset(matrix);
    }

    /**
     *
     * @param dataSet
     */
    protected void setXyDataset(SimpleMatrix dataSet) {
        xyDataset = new XYSeriesCollection();

        Map<String, SimpleMatrix> classMap = new MultiValueMap();
        for(int i = 0; i < dataSet.numRows(); ++i) {
            classMap.put(String.valueOf(dataSet.get(i, dataSet.numCols() - 1)),
                    dataSet.extractVector(true, i));
        }

        for(Map.Entry<String, SimpleMatrix> entry : classMap.entrySet()) {

            List<SimpleMatrix> values = (List<SimpleMatrix>) entry.getValue();
            XYSeries xySeries = new XYSeries(entry.getKey());

            values.stream().forEach((x) -> xySeries.add(x.get(0, 0), x.get(0, 1)));

            xyDataset.addSeries(xySeries);
        }
    }

    /**
     *
     * @param dataSet
     * @param x1Column
     * @param x2Column
     * @param yColumn
     */
    protected void setXyDataset(SimpleMatrix dataSet, int x1Column, int x2Column, int yColumn) {
        SimpleMatrix X = DataSetUtilities.getTrainingSet(dataSet, x1Column, x2Column);
        SimpleMatrix Y = DataSetUtilities.getAnswersSet(dataSet, yColumn);

        setXyDataset(DataSetUtilities.addColumns(X, Y));
    }

    protected abstract JPanel getChartPanel(String title);

    @Override
    public void plot() {
        this.pack();
        RefineryUtilities.centerFrameOnScreen(this);
        this.setVisible(true);
    }
}
