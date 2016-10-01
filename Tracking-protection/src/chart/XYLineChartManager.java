package chart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import utility.Constants;

public class XYLineChartManager extends XYChartManager {
	

	public XYLineChartManager(int flag, final String title) {
		super(flag, title);
		this.maxEnt = 5.0;

		if (this.flag == Constants.ENTROPY_FLAG) {
			series = new XYSeries("Entropy");
		}
		
		if (this.flag == Constants.TRACKING_SUCCESS_RATIO_FLAG){
			series = new XYSeries("Tracking Success Ratio");
		}
	}

	public void addData(double[] entropy) {
		for (int t = 0; t < entropy.length; t++) {
			series.add((double) (t + Constants.START_TIME), entropy[t]);
		}

		chartData = new XYSeriesCollection(series);
	}

	public void setChart(String title) {
		
		ValueAxis yAxis = null;
		String testParam = "";
		
		if (flag == Constants.ENTROPY_FLAG)
			testParam = "entropy";
		if (flag == Constants.TRACKING_SUCCESS_RATIO_FLAG)
			testParam = "probability";

		chart = ChartFactory.createXYLineChart(title, "time(s)", testParam, chartData, PlotOrientation.VERTICAL,
				true, true, false);
		XYPlot plot = (XYPlot) chart.getPlot();	
		yAxis = plot.getRangeAxis();
		
		
		if (flag == Constants.ENTROPY_FLAG)
			yAxis.setRange(0.0, maxEnt);
		if (flag == Constants.TRACKING_SUCCESS_RATIO_FLAG)
			yAxis.setRange(0.0, 1.0);

		chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(1200, 600));
		setContentPane(chartPanel);
	}

}
