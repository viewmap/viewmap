package chart;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;


public class XYChartManager extends ApplicationFrame{
	public JFreeChart chart;
	public ChartPanel chartPanel;
	public XYSeriesCollection chartData;
	public XYSeries series;
	
	public double maxEnt;
	public int flag;
	
	public XYChartManager(int flag, final String title) {
		super(title);
		this.flag = flag;
	}

	public void showChart() {
		pack();
		RefineryUtilities.centerFrameOnScreen(this);
		setVisible(true);
	}

}
