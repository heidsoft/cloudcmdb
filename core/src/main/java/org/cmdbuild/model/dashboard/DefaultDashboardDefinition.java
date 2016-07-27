package org.cmdbuild.model.dashboard;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class DefaultDashboardDefinition implements DashboardDefinition {

	public static ErrorMessageBuilder errors = new ErrorMessageBuilder();

	private String name;
	private String description;
	private LinkedHashMap<String, ChartDefinition> charts;
	private ArrayList<DashboardColumn> columns;
	private ArrayList<String> groups;

	public DefaultDashboardDefinition() {
		charts = new LinkedHashMap<String, ChartDefinition>();
		columns = new ArrayList<DashboardColumn>();
		groups = new ArrayList<String>();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(final String name) {
		this.name = name;
	}

	@Override
	public String getDescription() {
		return description;
	}
	
	@Override
	public void setDescription(final String description) {
		this.description = description;
	}
	
	// charts
	@Override
	public LinkedHashMap<String, ChartDefinition> getCharts() {
		return charts;
	}

	@Override
	public void setCharts(final LinkedHashMap<String, ChartDefinition> charts) {
		this.charts = charts;
	}

	@Override
	public ChartDefinition getChart(final String chartId) {
		ensureChartId(chartId);
		return this.charts.get(chartId);
	}

	@Override
	public void addChart(final String chartId, final ChartDefinition chart) {
		if (!this.charts.containsKey(chartId)) {
			this.putChart(chartId, chart);
		} else {
			throw new IllegalArgumentException(errors.duplicateChartIdForDashboard(chartId, this.getName()));
		}
	}

	@Override
	public void modifyChart(final String chartId, final ChartDefinition chart) {
		ensureChartId(chartId);
		this.putChart(chartId, chart);
	}

	@Override
	public ChartDefinition popChart(final String chartId) {
		ensureChartId(chartId);
		return this.charts.remove(chartId);
	}

	// columns
	@Override
	public ArrayList<DashboardColumn> getColumns() {
		return columns;
	}

	/**
	 * Does not make checks here because this method is used only by Jackson to
	 * de/serialize the columns so we we have not control to the order of json
	 * parsing so it's possible that it try to set the columns first, and then
	 * add the charts
	 */
	@Override
	public void setColumns(final ArrayList<DashboardColumn> columns) {
		this.columns = columns;
	}

	@Override
	public void addColumn(final DashboardColumn column) {
		ensureChartsConsistency(column);
		this.columns.add(column);
	}

	@Override
	public void removeColumn(final DashboardColumn column) {
		this.columns.remove(column);
	}

	// groups
	@Override
	public ArrayList<String> getGroups() {
		return groups;
	}

	@Override
	public void setGroups(final ArrayList<String> groups) {
		this.groups = groups;
	}

	@Override
	public void addGroup(final String group) {
		this.groups.add(group);
	}

	@Override
	public void removeGroup(final String group) {
		this.groups.remove(group);
	}

	/*
	 * support function to check that a chart is not null, before to add it in a
	 * dashboard
	 */
	private void putChart(final String chartId, final ChartDefinition chart) {
		if (chart != null) {
			this.charts.put(chartId, chart);
		} else {
			throw new IllegalArgumentException(errors.nullChart(this.getName()));
		}
	}

	/*
	 * support function to throw an exception if try to reach a chart that is
	 * not stored in the dashboard
	 */
	private void ensureChartId(final String chartId) {
		if (!this.charts.containsKey(chartId)) {
			throw new IllegalArgumentException(errors.notFoundChartIdForDashboard(chartId, this.getName()));
		}
	}

	/*
	 * support function called when add a column to be sure that the charts
	 * referred in the column are stored in the dashboard
	 */
	private void ensureChartsConsistency(final DashboardColumn column) {
		for (final String chartId : column.getCharts()) {
			if (!this.charts.containsKey(chartId)) {
				throw new IllegalArgumentException(errors.wrongChartInColumn(chartId, this.getName()));
			}
		}
	}

	/*
	 * A representation of a column of the dashboard to manage the references to
	 * the charts
	 */
	public static class DashboardColumn {
		private float width;
		private ArrayList<String> charts;

		public DashboardColumn() {
			width = 0;
			charts = new ArrayList<String>();
		}

		public float getWidth() {
			return width;
		}

		public void setWidth(final float width) {
			this.width = width;
		}

		public ArrayList<String> getCharts() {
			return charts;
		}

		public void setCharts(final ArrayList<String> charts) {
			this.charts = charts;
		}

		public void addChart(final String chartId) {
			this.charts.add(chartId);
		}

		public void removeChart(final String chartId) {
			this.charts.remove(chartId);
		}
	}

	/*
	 * to avoid an useless errors hierarchy define this object that build the
	 * errors messages These are used also in the tests to ensure that a right
	 * message is provided by the exception
	 */
	public static class ErrorMessageBuilder {
		public String duplicateChartIdForDashboard(final String chartId, final String dashboardName) {
			final String errorFormat = "The chart id %s is already used on dashboard %s";
			return String.format(errorFormat, chartId, dashboardName);
		}

		public String notFoundChartIdForDashboard(final String chartId, final String dashboardName) {
			final String errorFormat = "I'm not able to retrieve the chart with %s in the dashboard %s";
			return String.format(errorFormat, chartId, dashboardName);
		}

		public String nullChart(final String dashboardName) {
			final String errorFormat = "You are trying to add a null chart to dashboard %s";
			return String.format(errorFormat, dashboardName);
		}

		public String wrongChartInColumn(final String chartId, final String dashboardName) {
			final String errorForma = "You are trying to add a column with the chart %s that is not stored "
					+ "in dashboard %s";
			return String.format(errorForma, chartId, dashboardName);
		}
	}
}
