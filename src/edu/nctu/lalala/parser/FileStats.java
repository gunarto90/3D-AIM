package edu.nctu.lalala.parser;

import edu.nctu.lalala.common.Parameter;
import edu.nctu.lalala.util.MathUtility;
import edu.nctu.lalala.util.PrintUtility;

public class FileStats {
	private int numGps;
	private int numData;
	private int numDays;
	private int numActivities;
	private long fileSize;
	private int[] actDistributions;
	private double activityEntropy;

	public FileStats() {
		actDistributions = new int[Parameter.getTargetActivities().length];
		numDays = 1;
		numActivities = 0;
		fileSize = 0;
	}

	public int getNumGps() {
		return numGps;
	}

	public void incrementNumGps() {
		this.numGps++;
	}

	public int getNumData() {
		return numData;
	}

	public void setNumData(int numData) {
		this.numData = numData;
	}

	public void addNumData(int numData) {
		this.numData += numData;
	}

	public int getNumDays() {
		return numDays;
	}

	public void incrementNumDays() {
		this.numDays++;
	}

	public int getNumActivities() {
		if (numActivities == 0) {
			for (int i = 0; i < getActDistributions().length; i++) {
				if (getActDistributions()[i] > 0)
					incrementNumActivities();
			}
		}
		return numActivities;
	}

	public void incrementNumActivities() {
		this.numActivities++;
	}

	/**
	 * File size in MB (MegaBytes)
	 * 
	 * @return
	 */
	public long getFileSize() {
		return fileSize / 1024 / 1024;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public void addFileSize(long fileSize) {
		this.fileSize += fileSize;
	}

	public int[] getActDistributions() {
		return actDistributions;
	}

	public void incrementActivity(int index) {
		if (index < 0 || index >= actDistributions.length)
			return;
		actDistributions[index]++;
	}

	public double getActivityEntropy() {
		if (activityEntropy == 0)
			calculateActivityEntropy();
		return activityEntropy;
	}

	private void calculateActivityEntropy() {
		int frequency = 0;
		for (int i = 0; i < getActDistributions().length; i++) {
			frequency += getActDistributions()[i];
		}
		double entropy = MathUtility.getInstance().calculateEntropy(getActDistributions(), frequency);
		this.activityEntropy = entropy;
	}

	public String getActString() {
		return PrintUtility.getInstance().printArray(getActDistributions());
	}

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
		sb.append("Num of gps : " + getNumGps());
		sb.append("\n");
		sb.append(getActString());
		return sb.toString();
	}

	public String generateColumnReport(boolean withHeader) {
		StringBuilder sb = new StringBuilder();
		if (withHeader) {
			sb.append("A.Entropy").append("\t");
			sb.append("File size").append("\t");
			sb.append("Num of days").append("\t");
			sb.append("Num of acts").append("\t");
			sb.append("Num of data").append("\t");
			sb.append("Num of gps").append("\t");
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
		sb.append(getNumGps()).append("\t");
		for (int i = 0; i < getActDistributions().length; i++) {
			sb.append(getActDistributions()[i]);
			if (i < getActDistributions().length - 1)
				sb.append("\t");
		}
		return sb.toString();
	}

}
