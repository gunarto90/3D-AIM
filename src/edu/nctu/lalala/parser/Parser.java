package edu.nctu.lalala.parser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import edu.nctu.lalala.common.Parameter;
import edu.nctu.lalala.enums.TrainingMode;

enum CSV_Type {
	Accel, GPS
}

public class Parser {

	List<Integer> trainingIndex = new ArrayList<>();

	public void ProcessAllUsers() {
		System.out.println(Parameter.getWorkingDirectory());
		for (int index = 0; index < Parameter.getUsers().length; index++) {
			String userId = Parameter.getUsers()[index];
			System.out.println(userId);
			FileStats stats = InitializeHardware(userId);
			System.out.println(stats.generateColumnReport(false));
		}
	}

	public FileStats InitializeHardware(String userId) {
		File HardwareFile = new File(Parameter.HARDWARE_PATH + userId);
		ArrayList<String> HardwareFileList = new ArrayList<String>();
		String d1, d2;
		d1 = null;
		FileStats stats = new FileStats();

		// System.out.println(HardwareFile.getAbsolutePath());
		if (HardwareFile.isDirectory()) // Just to make sure no error happens
		{
			String[] s = HardwareFile.list(); // List
			// System.out.println("size : " + s.length);
			for (int i = 0; i < s.length; i++) {
				HardwareFileList.add(s[i]);
			}
		}
		
		if (Parameter.IS_GENERATING_CSV_FILES) {
			// First "refresh" the file (delete them)
			 refreshCSV(userId);
		}

		for (int i = 0; i < HardwareFileList.size(); i++) {
			// System.out.println(i + ": " + HardwareFileList.get(i));
			// Count num of days by observing filename
			d2 = d1;
			d1 = HardwareFileList.get(i).split(" ")[0];
			if (i > 0) {
				if (!d1.equals(d2)) {
					stats.incrementNumDays();
				}
			}
			String filename = userId + "\\" + HardwareFileList.get(i);
			String hardwareFileName = Parameter.HARDWARE_PATH + filename;
			stats.addFileSize(new File(hardwareFileName).length());
			// Make directories first
			generateDirectories(userId);
			// Initialize hardware
			InitializeHardware(hardwareFileName, filename, userId, stats);
		}

		return stats;
	}

	private void generateDirectories(String userId) {
		File f = null;
		f = new File(Parameter.HARDWARE_TRAINING_PATH + userId);
		f.mkdirs();
		f = new File(Parameter.HARDWARE_TESTING_PATH + userId);
		f.mkdirs();
		f = new File(Parameter.CSV_OUTPUT);
		f.mkdirs();
	}

	public void InitializeHardware(String hardwareFileName, String fileName, String userId, FileStats stats) {
		try {
			InitializeHardware(new FileReader(hardwareFileName), fileName, userId, stats);
		} catch (Exception e) {
			System.err.println("[ERR-Parser.Hardware]: " + hardwareFileName + " is broken !");
		}
	}

	private void refreshCSV(String userId) {
		File f = null;
		f = new File(Parameter.CSV_OUTPUT + userId + "_accel.csv");
		if (f.exists())
			f.delete();
		f = new File(Parameter.CSV_OUTPUT + userId + "_gps.csv");
		if (f.exists())
			f.delete();
	}

	@SuppressWarnings("unchecked")
	public void InitializeHardware(Reader hardwareReader, String fileName, String userId, FileStats stats)
			throws IOException, ParseException {
		JSONParser jsonParser = new JSONParser();
		JSONArray arr = (JSONArray) jsonParser.parse(hardwareReader);
		String activity = null;
		Iterator<?> iterator = arr.iterator();
		int iteratorCounter = 0;

		// System.out.println("JSON unit: "+arr.size());
		random(arr.size());
		// crossValidation(arr.size());
		JSONArray trainingMainArray = new JSONArray();
		JSONArray testingMainArray = new JSONArray();

		List<String> accel_csv_list = new ArrayList<>();
		List<String> gps_csv_list = new ArrayList<>();
		try {
			while (iterator.hasNext()) {
				JSONObject parse = (JSONObject) iterator.next();

				JSONArray accel = (JSONArray) parse.get("Accel");
				String lifelable = (String) parse.get("lifelable");
				String lifelabel = (String) parse.get("lifelabel");
				JSONArray gps = (JSONArray) parse.get("GPS");

				// Because version 1 use "lifelable"
				if (lifelable != null)
					activity = lifelable;
				else if (lifelabel != null)
					activity = lifelabel;

				JSONObject unitObj = new JSONObject();
				unitObj.put("Accel", accel);
				unitObj.put("GPS", gps);
				unitObj.put("lifelabel", activity);

				Iterator<?> tempIterator = null;
				JSONObject temp = null;
				// Accel
				tempIterator = accel.iterator();
				while (tempIterator.hasNext()) {
					temp = (JSONObject) tempIterator.next();
					accel_csv_list.add(buildCSV(userId, activity, temp, CSV_Type.Accel));
				}
				// GPS
				if (gps.size() > 0) {
					tempIterator = gps.iterator();
					while (tempIterator.hasNext()) {
						temp = (JSONObject) tempIterator.next();
						gps_csv_list.add(buildCSV(userId, activity, temp, CSV_Type.GPS));
					}
				}

				// Match index
				for (int i = 0; i < Parameter.getTargetActivities().length; i++) {
					if (Parameter.getTargetActivities()[i].equals(activity)) {
						stats.incrementActivity(i, accel.size());
						// System.out.println(i + ":" + activity);
						break;
					}
				}

				if (Parameter.MODE == TrainingMode.Default) {
					if (trainingIndex.contains(iteratorCounter))
						trainingMainArray.add(unitObj);
					else
						testingMainArray.add(unitObj);
				} else if (Parameter.MODE == TrainingMode.CrossValidation) {
					if (trainingIndex.contains(iteratorCounter))
						testingMainArray.add(unitObj);
					else
						trainingMainArray.add(unitObj);
				}

				iteratorCounter++;
			} // End of iterator
				// Add statistics (precise)
			stats.addNumData(accel_csv_list.size());
			stats.addNumGps(gps_csv_list.size());
			// Write to file
			Writer writer = null;
			if (Parameter.IS_GENERATING_TRAINING_FILES) {
				writer = new FileWriter(Parameter.HARDWARE_TRAINING_PATH + fileName);
				writer.write(trainingMainArray.toString());
				writer.close();
			}
			if (Parameter.IS_GENERATING_TESTING_FILES) {
				writer = new FileWriter(Parameter.HARDWARE_TESTING_PATH + fileName);
				writer.write(testingMainArray.toString());
				writer.close();
			}
		} catch (Exception e) {
			System.err.println("[ERR-Parser.InitHardware]: " + e.getMessage());
		}
	}

	public void random(int range) {
		Random rd = new Random();
		int numberOfTraining = (int) ((double) range * Parameter.getTrainingRatio());
		// System.out.println("Number of training data: " + numberOfTraining);
		while (trainingIndex.size() < numberOfTraining) {
			int n = rd.nextInt(range);
			if (trainingIndex.contains(n))
				continue;
			else
				trainingIndex.add(n);
		}
		Collections.sort(trainingIndex);
	}

	private String buildCSVHeader(String userId, String activity) {
		StringBuilder sb = new StringBuilder();
		sb.append(userId).append(',');
		sb.append(activity).append(',');
		return sb.toString();
	}

	private String buildCSV(String userId, String activity, JSONObject parse, CSV_Type type) {
		// Normalize time
		// Build the string
		String header = buildCSVHeader(userId, activity);
		StringBuilder sb = new StringBuilder();
		sb.append(header);
		if (type == CSV_Type.Accel) {
			sb.append(parse.get("time")).append(',');
			sb.append(parse.get("X")).append(',');
			sb.append(parse.get("Y")).append(',');
			sb.append(parse.get("Z"));
		} else if (type == CSV_Type.GPS) {
			sb.append(parse.get("time")).append(',');
			sb.append(parse.get("X")).append(',');
			sb.append(parse.get("Y")).append(',');
			sb.append(parse.get("Speed"));
		}
		sb.append('\n');
		if (Parameter.IS_GENERATING_CSV_FILES) {
			writeCSVtoFile(userId, type, sb);
		}
		return sb.toString();
	}

	private void writeCSVtoFile(String userId, CSV_Type type, StringBuilder sb) {
		Writer writer = null;
		try {
			if (type == CSV_Type.Accel) {
				writer = new FileWriter(Parameter.CSV_OUTPUT + userId + "_accel.csv", true);
			} else if (type == CSV_Type.GPS)
				writer = new FileWriter(Parameter.CSV_OUTPUT + userId + "_gps.csv", true);
			writer.append(sb.toString());
			writer.close();
		} catch (IOException e) {
			// e.printStackTrace();
		}
	}
}
