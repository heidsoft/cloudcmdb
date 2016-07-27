package org.cmdbuild.model.dashboard;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.cmdbuild.model.dashboard.DefaultDashboardDefinition.DashboardColumn;

public interface DashboardDefinition {

	String getName();

	void setName(String name);

	String getDescription();

	void setDescription(String description);

	// charts
	LinkedHashMap<String, ChartDefinition> getCharts();

	void setCharts(LinkedHashMap<String, ChartDefinition> charts);

	ChartDefinition getChart(String chartId);

	void addChart(String chartId, ChartDefinition chart);

	void modifyChart(String chartId, ChartDefinition chart);

	ChartDefinition popChart(String chartId);

	// columns
	ArrayList<DashboardColumn> getColumns();

	/**
	 * Does not make checks here because this method is used only by Jackson to
	 * de/serialize the columns so we we have not control to the order of json
	 * parsing so it's possible that it try to set the columns first, and then
	 * add the charts
	 */
	void setColumns(ArrayList<DashboardColumn> columns);

	void addColumn(DashboardColumn column);

	void removeColumn(DashboardColumn column);

	// groups
	ArrayList<String> getGroups();

	void setGroups(ArrayList<String> groups);

	void addGroup(String group);

	void removeGroup(String group);

}