package chart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;

import utility.Constants;

public class XYBarChartManager extends XYChartManager {
	private CategoryDataset barChartData;
	
	public XYBarChartManager(int flag, String title) {
		super(flag, title);
		
		series = new XYSeries("Probability Distribution");
		maxEnt = 1.0;

	}

	public void addBarChartData(double[] entropy) {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		for (int v = 0; v < entropy.length; v++) {
			dataset.addValue(entropy[v], "probability", (v + 1) + "");
		}
		barChartData = dataset;
	}

	public void setChart(String title) {

		ValueAxis yAxis = null;
		String testParam = "";

		if (flag == Constants.PROBABILITY_DISTRIBUTION_FLAG) {
			chart = ChartFactory.createBarChart(title, "Vehicle ID", testParam, barChartData, PlotOrientation.VERTICAL,
					false, true, false);
			CategoryPlot plot = (CategoryPlot) chart.getPlot();
			yAxis = plot.getRangeAxis();
		}

		if (flag == Constants.PROBABILITY_DISTRIBUTION_FLAG)
			yAxis.setRange(0.0, 1.0);

		chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(800, 400));
		setContentPane(chartPanel);
	}
}
