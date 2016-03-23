package edu.nctu.lalala.parser;

import edu.nctu.lalala.common.Parameter;
import edu.nctu.lalala.util.MathUtility;
import edu.nctu.lalala.util.PrintUtility;

public abstract class FileStats {
	protected int numData;
	protected int numDays;
	protected int numActivities;
	protected long fileSize;
	protected int[] actDistributions;
	protected double activityEntropy;
	
	public FileStats() {
		actDistributions = new int[Parameter.getTargetActivities().length];
		numDays = 1;
		numActivities = 0;
		fileSize = 0;
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
	
	public void incrementActivity(int index, int amount) {
		if (index < 0 || index >= actDistributions.length)
			return;
		actDistributions[index] += amount;
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
	
	public abstract String generateReport();
	public abstract String generateColumnReport(boolean withHeader);
}
