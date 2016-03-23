package edu.nctu.lalala.parser;

import edu.nctu.lalala.common.Parameter;
import edu.nctu.lalala.util.MathUtility;
import edu.nctu.lalala.util.PrintUtility;

public class HardwareStats extends FileStats {
	private int numGps;	

	public HardwareStats() {
		super();
	}

	public int getNumGps() {
		return numGps;
	}

	public void incrementNumGps() {
		this.numGps++;
	}

	public void addNumGps(int numGps) {
		this.numGps += numGps;
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
		sb.append("Num of gps : " + getNumGps());
		sb.append("\n");
		sb.append(getActString());
		return sb.toString();
	}

	@Override
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
