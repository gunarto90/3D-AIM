package edu.nctu.lalala.util;

public class MathUtility {
	private MathUtility() {
	}

	private static final MathUtility singleton = new MathUtility();

	public static MathUtility getInstance() {
		return singleton;
	}

	/**
	 * Calculate average of a list
	 * 
	 * @param data
	 * @return
	 */
	public Double calculateAverage(Double... data) {
		Double mean = 0.0;
		if (data.length > 0) {
			for (Double x : data) {
				mean += x;
			}
			mean = mean / data.length;
		}
		return mean;
	}

	/**
	 * Calculate stdev of a list
	 * 
	 * @param inst
	 * @param average
	 * @return
	 */
	public Double calculateStdev(Double average, Double... data) {
		Double stdev = 0.0;
		if (data.length > 0) {
			for (int i = 0; i < data.length; i++) {
				stdev += Math.pow(data[i] - average, 2);
			}
			stdev /= data.length;
		}
		return stdev;
	}

	/**
	 * Calculate quartile of a list
	 * 
	 * @param data
	 * @return
	 */
	public Double[] calculateQuartile(Double... data) {
		Double[] q = new Double[3]; // Q1, Q2, Q3
		final int QUARTILE = 4;
		for (int i = 1; i < QUARTILE; i++) {
			int pos = (data.length * i / QUARTILE) + 1;
			q[i - 1] = data[pos];
		}
		return q;
	}

	public double calculateEntropy(double[] counter, double frequency) {
		if (frequency == 0)
			return 0;
		double entropy = 0;
		double[] p = new double[counter.length];
		for (int i = 0; i < counter.length; i++) {
			p[i] = counter[i] / frequency;
		}
		for (int i = 0; i < p.length; i++) {
			if (p[i] == 0.0F)
				continue;
			entropy -= p[i] * (Math.log(p[i]) / Math.log(p.length));
		}
		return entropy;
	}

	public double calculateEntropy(int[] counter, int frequency) {
		if (frequency == 0)
			return 0;
		double entropy = 0;
		double[] p = new double[counter.length];
		for (int i = 0; i < counter.length; i++) {
			p[i] = (double) counter[i] / frequency;
		}
		for (int i = 0; i < p.length; i++) {
			if (p[i] == 0.0F)
				continue;
			entropy -= p[i] * (Math.log(p[i]) / Math.log(p.length));
		}
		return entropy;
	}

	public double calculateEarthDistance(double raw_lon1, double raw_lat1, double raw_lon2, double raw_lat2) {
		final double EARTH_RADIUS = 6378.137;

		double lat1 = rad(raw_lat1);
		double lat2 = rad(raw_lat2);
		double lon1 = rad(raw_lon1);
		double lon2 = rad(raw_lon2);

		double a = lat1 - lat2;
		double b = lon1 - lon2;
		double s = 2d * Math.asin(Math.sqrt(
				Math.pow(Math.sin(a / 2d), 2d) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(b / 2d), 2d)));

		s = s * EARTH_RADIUS;
		s = Math.round(s * 10000d) / 10000d;

		return s;
	}

	public double rad(double a) {
		return a * Math.PI / 180.0;
	}
}
