package edu.nctu.lalala.parser;

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
	Accel, GPS, App
}

enum Init_Type {
	Hardware, Software
}

public class Parser {

	List<Integer> trainingIndex = new ArrayList<>();

	public void processAllUsers() {
		System.out.println(Parameter.getWorkingDirectory());
		for (int index = 0; index < Parameter.getUsers().length; index++) {
			String userId = Parameter.getUsers()[index];
			System.out.println(userId);
//			FileStats hardwareStats = initializeData(userId, Init_Type.Hardware);
//			System.out.println(hardwareStats.generateColumnReport(false));
			FileStats softwareStats = initializeData(userId, Init_Type.Software);
			System.out.println(softwareStats.generateColumnReport(true));
//			System.out.println(softwareStats.generateReport());
		}
	}

	public FileStats initializeData(String userId, Init_Type type) {
		File file = null;
		if (type == Init_Type.Hardware)
			file = new File(Parameter.HARDWARE_PATH + userId);
		else if (type == Init_Type.Software)
			file = new File(Parameter.SOFTWARE_PATH + userId);

		ArrayList<String> fileList = new ArrayList<String>();
		String d1, d2;
		d1 = null;
		FileStats stats = null;
		if (type == Init_Type.Hardware)
			stats = new HardwareStats();
		else if(type == Init_Type.Software)
			stats = new SoftwareStats();

		if (Parameter.IS_GENERATING_CSV_FILES) {
			// First "refresh" the file (delete them)
			refreshCSV(userId, type);
		}

		if (file.isDirectory()) // Just to make sure no error happens
		{
			String[] s = file.list();
			for (int i = 0; i < s.length; i++) {
				fileList.add(s[i]);
			}
		}

		for (int i = 0; i < fileList.size(); i++) {
			// Count num of days by observing filename
			d2 = d1;
			d1 = fileList.get(i).split(" ")[0];
			if (i > 0) {
				if (!d1.equals(d2)) {
					stats.incrementNumDays();
				}
			}
			String filename = userId + "\\" + fileList.get(i);
			String targetFileName = null;
			if (type == Init_Type.Hardware)
				targetFileName = Parameter.HARDWARE_PATH + filename;
			else if (type == Init_Type.Software)
				targetFileName = Parameter.SOFTWARE_PATH + filename;
			stats.addFileSize(new File(targetFileName).length());
			// Make directories first
			generateDirectories(userId, type);
			// Initialize hardware
			initializeData(targetFileName, filename, userId, stats, type);
		}
		return stats;
	}

	private void generateDirectories(String userId, Init_Type type) {
		File f = null;
		if (type == Init_Type.Hardware) {
			f = new File(Parameter.HARDWARE_TRAINING_PATH + userId);
			f.mkdirs();
			f = new File(Parameter.HARDWARE_TESTING_PATH + userId);
			f.mkdirs();			
		}
		else if(type == Init_Type.Software)
		{
			f = new File(Parameter.SOFTWARE_TRAINING_PATH + userId);
			f.mkdirs();
			f = new File(Parameter.SOFTWARE_TESTING_PATH + userId);
			f.mkdirs();
		}
		// csv folder
		f = new File(Parameter.CSV_OUTPUT);
		f.mkdirs();
	}

	public void initializeData(String targetFileName, String fileName, String userId, FileStats stats,
			Init_Type type) {
		try {
			initializeData(new FileReader(targetFileName), fileName, userId, stats, type);
		} catch (Exception e) {
			System.err.println("[ERR-Parser.initData]: " + targetFileName + " is broken !");
		}
	}

	private void refreshCSV(String userId, Init_Type type) {
		File f = null;
		if (type == Init_Type.Hardware) {
			f = new File(Parameter.CSV_OUTPUT + userId + "_accel.csv");
			if (f.exists())
				f.delete();
			f = new File(Parameter.CSV_OUTPUT + userId + "_gps.csv");
			if (f.exists())
				f.delete();
		} else if(type == Init_Type.Software)
		{
			f = new File(Parameter.CSV_OUTPUT + userId + "_soft.csv");
			if (f.exists())
				f.delete();
		}
	}

	@SuppressWarnings("unchecked")
	public void initializeData(Reader reader, String fileName, String userId, FileStats stats, Init_Type type)
			throws IOException, ParseException {
		JSONParser jsonParser = new JSONParser();
		JSONArray arr = (JSONArray) jsonParser.parse(reader);
		String activity = null;
		Iterator<?> iterator = arr.iterator();
		int iteratorCounter = 0;

		// System.out.println("JSON unit: "+arr.size());
		random(arr.size());
		// crossValidation(arr.size());
		JSONArray trainingArray = new JSONArray();
		JSONArray testingArray = new JSONArray();

		List<String> accelCsvList = new ArrayList<>();
		List<String> gpsCsvList = new ArrayList<>();
		List<String> appCsvList = new ArrayList<>();
		
		try {
			while (iterator.hasNext()) {
				JSONObject parse = (JSONObject) iterator.next();
				JSONObject unitObj = new JSONObject();
				Iterator<?> tempIterator = null;
				JSONObject temp = null;

				// Get the activity label from data
				String lifelable = (String) parse.get("lifelable");
				String lifelabel = (String) parse.get("lifelabel");
				// Because version 1 use "lifelable"
				if (lifelable != null)
					activity = lifelable;
				else if (lifelabel != null)
					activity = lifelabel;
				unitObj.put("lifelabel", activity);

				if (type == Init_Type.Hardware) {
					JSONArray accel = (JSONArray) parse.get("Accel");
					JSONArray gps = (JSONArray) parse.get("GPS");
					unitObj.put("Accel", accel);
					unitObj.put("GPS", gps);
					// Accel
					tempIterator = accel.iterator();
					while (tempIterator.hasNext()) {
						temp = (JSONObject) tempIterator.next();
						accelCsvList.add(buildCSV(userId, activity, temp, CSV_Type.Accel));
					}
					// GPS
					if (gps.size() > 0) {
						tempIterator = gps.iterator();
						while (tempIterator.hasNext()) {
							temp = (JSONObject) tempIterator.next();
							gpsCsvList.add(buildCSV(userId, activity, temp, CSV_Type.GPS));
						}
					}
					// Match index
					for (int i = 0; i < Parameter.getTargetActivities().length; i++) {
						if (Parameter.getTargetActivities()[i].equals(activity)) {
							stats.incrementActivity(i, accel.size());
							break;
						}
					}
				} else if (type == Init_Type.Software) {
					JSONArray app = (JSONArray) parse.get("App");
					String time = parse.get("time").toString();
					unitObj.put("App", app);
					unitObj.put("time", time);
					tempIterator = app.iterator();
					while (tempIterator.hasNext()) {
						temp = (JSONObject) tempIterator.next();
						SoftwareStats soft = (SoftwareStats) stats;
						soft.incrementAppStats(temp.get("name").toString());
						appCsvList.add(buildCSV(userId, activity, temp, CSV_Type.App, time));
					}
					// Match index
					for (int i = 0; i < Parameter.getTargetActivities().length; i++) {
						if (Parameter.getTargetActivities()[i].equals(activity)) {
							stats.incrementActivity(i);
							break;
						}
					}
				}

				if (Parameter.MODE == TrainingMode.Default) {
					if (trainingIndex.contains(iteratorCounter))
						trainingArray.add(unitObj);
					else
						testingArray.add(unitObj);
				} else if (Parameter.MODE == TrainingMode.CrossValidation) {
					if (trainingIndex.contains(iteratorCounter))
						testingArray.add(unitObj);
					else
						trainingArray.add(unitObj);
				}

				iteratorCounter++;
			} // End of iterator

			if (type == Init_Type.Hardware) {
				HardwareStats hard = (HardwareStats) stats;
				// Add statistics (precise)
				stats.addNumData(accelCsvList.size());
				hard.addNumGps(gpsCsvList.size());
			}
			else if(type == Init_Type.Software)
			{
				stats.addNumData(arr.size());
			}
			// Write to file
			Writer writer = null;

			if (Parameter.IS_GENERATING_TRAINING_FILES) {
				if (type == Init_Type.Hardware)
					writer = new FileWriter(Parameter.HARDWARE_TRAINING_PATH + fileName);
				else if (type == Init_Type.Software)
					writer = new FileWriter(Parameter.SOFTWARE_TRAINING_PATH + fileName);
				writer.write(trainingArray.toString());
				writer.close();
			}
			if (Parameter.IS_GENERATING_TESTING_FILES) {
				if (type == Init_Type.Hardware)
					writer = new FileWriter(Parameter.HARDWARE_TESTING_PATH + fileName);
				else if(type == Init_Type.Software)
					writer = new FileWriter(Parameter.SOFTWARE_TESTING_PATH + fileName);
				writer.write(testingArray.toString());
				writer.close();
			}

		} catch (Exception e) {
			System.err.println("[ERR-Parser.InitData]: " + e.getMessage());
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

	private String buildCSVFront(String userId, String activity) {
		StringBuilder sb = new StringBuilder();
		sb.append(userId).append(',');
		sb.append(activity).append(',');
		return sb.toString();
	}

	private String buildCSV(String userId, String activity, JSONObject parse, CSV_Type type, String... others) {
		// Normalize time
		// Build the string
		String header = buildCSVFront(userId, activity);
		StringBuilder sb = new StringBuilder();
		sb.append(header);
		if (type == CSV_Type.Accel) {
			sb.append(parse.get("time")).append(',');
			sb.append(parse.get("X")).append(',');
			sb.append(parse.get("Y")).append(',');
			sb.append(parse.get("Z"));
		} else if (type == CSV_Type.GPS) {
			sb.append(parse.get("time")).append(',');
			sb.append(parse.get("Y")).append(',');	// latitude
			sb.append(parse.get("X")).append(',');	// longitude
			sb.append(parse.get("Speed"));
		} else if(type == CSV_Type.App)
		{
			sb.append(parse.get("name"));
		}
		if(others.length >0)
		{
			for(String s: others)
			{
				sb.append(',').append(s);
			}
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
			if (type == CSV_Type.Accel)
				writer = new FileWriter(Parameter.CSV_OUTPUT + userId + "_accel.csv", true);
			else if (type == CSV_Type.GPS)
				writer = new FileWriter(Parameter.CSV_OUTPUT + userId + "_gps.csv", true);
			else if(type == CSV_Type.App)
				writer = new FileWriter(Parameter.CSV_OUTPUT + userId + "_soft.csv", true);
			writer.append(sb.toString());
			writer.close();
		} catch (IOException e) {
			// e.printStackTrace();
		}
	}
}
