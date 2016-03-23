package edu.nctu.lalala.parser;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import edu.nctu.lalala.common.Parameter;

class AppStats {
	private String appName;
	private int counter;

	public AppStats(String appName) {
		this.setAppName(appName);
		setCounter(1);
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public int getCounter() {
		return counter;
	}

	public void setCounter(int counter) {
		this.counter = counter;
	}

	public void addCounter(int counter) {
		this.counter += counter;
	}
	@Override
	public String toString() {
		
		return String.format("%s\t%d", getAppName(), getCounter());
	}
}

public class SoftwareStats extends FileStats {
	Map<String, AppStats> appList;

	public SoftwareStats() {
		super();
		appList = new HashMap<String, AppStats>();
	}

	public AppStats getAppStats(String appName) {
		return appList.get(appName);
	}

	public void incrementAppStats(String appName) {
		incrementAppStats(appName, 1);
	}

	public void incrementAppStats(String appName, int counter) {
		AppStats app = getAppStats(appName);
		if (app == null) {
			app = new AppStats(appName);
			appList.put(appName, app);
		} else {
			app.addCounter(counter);
		}
	}

	@Override
	public String generateReport() {
		StringBuilder sb = new StringBuilder();
		sb.append("A.Entropy  : " + getActivityEntropy());
		sb.append("\n");
		sb.append("File size  : " + getFileSize());
		sb.append("\n");
		sb.append("Num of days: " + getNumDays());
		sb.append("\n");
		sb.append("Num of acts: " + getNumActivities());
		sb.append("\n");
		sb.append("Num of data: " + getNumData());
		sb.append("\n");
		sb.append(getActString());
		sb.append("\n");
		sb.append("Unique apps: ").append('\n');
		Iterator<AppStats> iterator = appList.values().iterator();
		while(iterator.hasNext())
		{
			sb.append(iterator.next().toString()).append('\n');
		}		
		return sb.toString();
	}

	@Override
	public String generateColumnReport(boolean withHeader) {
		StringBuilder sb = new StringBuilder();
		if(withHeader)
		{
			sb.append("A.Entropy").append("\t");
			sb.append("File size").append("\t");
			sb.append("Num of days").append("\t");
			sb.append("Num of acts").append("\t");
			sb.append("Num of data").append("\t");
			sb.append("Num of unique app").append("\t");
			for (int i = 0; i < getActDistributions().length; i++) {
				sb.append(Parameter.getTargetActivities()[i]);
				if (i < getActDistributions().length - 1)
					sb.append("\t");
			}
			sb.append("\n");
		}
		sb.append(getActivityEntropy()).append("\t");
		sb.append(getFileSize()).append("\t");
		sb.append(getNumDays()).append("\t");
		sb.append(getNumActivities()).append("\t");
		sb.append(getNumData()).append("\t");
		sb.append(this.appList.size()).append("\t");
		for (int i = 0; i < getActDistributions().length; i++) {
			sb.append(getActDistributions()[i]);
			if (i < getActDistributions().length - 1)
				sb.append("\t");
		}
		return sb.toString();
	}

}
