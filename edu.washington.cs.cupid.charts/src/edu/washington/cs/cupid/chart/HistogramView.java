package edu.washington.cs.cupid.chart;

import java.awt.Color;
import java.util.Set;

import org.eclipse.swt.widgets.Display;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;

import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.chart.internal.ChartViewPart;

public class HistogramView extends ChartViewPart{
	
	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "edu.washington.cs.cupid.chart.HistogramView";
	
	/**
	 * The constructor.
	 */
	public HistogramView() {
	}

	@Override
	protected void buildChart(){
		
		HistogramDataset dataset = new HistogramDataset();
		dataset.setType(HistogramType.FREQUENCY);
		
		double[] values = new double[results.values().size()];
		
		Number[] arr = (Number[]) results.values().toArray(new Number[]{});
		
		for (int i = 0; i < values.length; i++){
			values[i] = arr[i].doubleValue();
		}
		
		dataset.addSeries("Histogram", values, 10);
		
		// create the chart
		final JFreeChart chart = ChartFactory.createHistogram(
				null, // title
				null, // xAxisLabel
				null, // yAxisLabel
			    dataset,
			    PlotOrientation.VERTICAL,
			    false, // legend
			    false, // tooltips, 
			    false); // urls 		
		
		Plot plot = chart.getPlot();
		plot.setNoDataMessage("No data available");
		plot.setBackgroundPaint(Color.WHITE);
		plot.setOutlinePaint(Color.WHITE);
		
		// update the display
		Display.getDefault().asyncExec(new Runnable(){
			@Override
			public void run() {
				frame.removeAll();
				frame.add(new ChartPanel(chart));
				frame.validate();
			}
		});
	}

	@Override
	protected String getName() {
		return "Histogram";
	}

	@Override
	protected Set<TypeToken<?>> accepts() {
		return Sets.<TypeToken<?>>newHashSet(
				TypeToken.of(Number.class),
				TypeToken.of(int.class),
				TypeToken.of(double.class),
				TypeToken.of(float.class),
				TypeToken.of(long.class));
	}
}