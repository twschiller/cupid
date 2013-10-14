package edu.washington.cs.cupid.chart;

import java.awt.Color;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.widgets.Display;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;

import edu.washington.cs.cupid.chart.internal.ChartViewPart;

public class PieChartView extends ChartViewPart{

	// TODO reduce flicker: http://www.eclipse.org/articles/article.php?file=Article-Swing-SWT-Integration/index.html
	// TODO add job cancellation smoothing (like for the inspector view)
	
	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "edu.washington.cs.cupid.chart.PieChartView";

	/**
	 * The constructor.
	 */
	public PieChartView() {
	}

	@Override
	protected void buildChart(){
		
		// determine relative frequencies
		DefaultPieDataset dataset = new DefaultPieDataset();
		
		Collection<Object> unrolled = Lists.newArrayList();
		
		if (super.showListResults){
			// flatten the results if the capability returns a list
			for (Object value : results.values()){
				Collection<?> valueSet = (Collection<?>) value;
				unrolled.addAll(valueSet);
			}
		}else{
			unrolled = results.values();
		}
		
		Set<?> unique = Sets.newHashSet(unrolled);
		
		for (Object result : unique){
			dataset.setValue(result.toString(), Iterables.frequency(unrolled, result) / (double) unrolled.size());
		}
		
		// create the chart
		final JFreeChart chart = ChartFactory.createPieChart(
				null, // no title (title is already on view part)
				dataset,
				true, // include legend
				false, // include tool tips
				false // include URLs
		);
		
		PiePlot plot = (PiePlot) chart.getPlot();
		plot.setNoDataMessage("No data available");
		plot.setBackgroundPaint(Color.WHITE);
		plot.setOutlinePaint(Color.WHITE);
	    plot.setShadowPaint(Color.WHITE);
		
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
		return "Pie Chart";
	}
	
	@Override
	protected Set<TypeToken<?>> accepts() {
		return Sets.<TypeToken<?>>newHashSet(TypeToken.of(Object.class));
	}
}