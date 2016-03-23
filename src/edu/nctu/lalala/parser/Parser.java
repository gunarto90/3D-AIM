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

	/**
	 * if true, generate some sample in addition to original csv
	 */
	public static boolean IS_GENERATING_SAMPLE_FILES = false;
	public static int SAMPLE_NUMBER = 300;
	public static String SAMPLE_PREFIX = "sample_" + SAMPLE_NUMBER + "_";
	static List<List<String>> ACC_SAMPLE_CSV = new ArrayList<>();
	static List<List<String>> GPS_SAMPLE_CSV = new ArrayList<>();
	static List<List<String>> APP_SAMPLE_CSV = new ArrayList<>();

	static String ACC_CSV_NAME;
	static String GPS_CSV_NAME;
	static String APP_CSV_NAME;

	public Parser() {
		if (IS_GENERATING_SAMPLE_FILES) {
			ACC_CSV_NAME = "%s%s%s_accel.csv";
			GPS_CSV_NAME = "%s%s%s_gps.csv";
			APP_CSV_NAME = "%s%s%s_soft.csv";
		} else {
			ACC_CSV_NAME = "%s%s_accel.csv";
			GPS_CSV_NAME = "%s%s_gps.csv";
			APP_CSV_NAME = "%s%s_soft.csv";
		}
		for (int i = 0; i < Parameter.getTargetActivities().length; i++) {
			ACC_SAMPLE_CSV.add(new ArrayList<>());
			GPS_SAMPLE_CSV.add(new ArrayList<>());
			APP_SAMPLE_CSV.add(new ArrayList<>());
		}

		// System.out.println(PrintUtility.getInstance().getClassName());
	}

	List<Integer> trainingIndex = new ArrayList<>();

	@SuppressWarnings("unused")
	public void processAllUsers() {
		System.out.println(Parameter.getWorkingDirectory());
		for (int index = 0; index < Parameter.getUsers().length; index++) {
			String userId = Parameter.getUsers()[index];
			System.out.println(userId);
			FileStats hardwareStats = initializeData(userId, Init_Type.Hardware);
			// System.out.println(hardwareStats.generateColumnReport(false));
			FileStats softwareStats = initializeData(userId, Init_Type.Software);
			// System.out.println(softwareStats.generateColumnReport(false));
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
		else if (type == Init_Type.Software)
			stats = new SoftwareStats();

		if (Parameter.IS_GENERATING_CSV_FILES || IS_GENERATING_SAMPLE_FILES) {
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

		// Sampling
		if (IS_GENERATING_SAMPLE_FILES) {
			if (type == Init_Type.Hardware) {
				// Accel
				reduceSampleCSV(ACC_SAMPLE_CSV);
				for (List<String> list : ACC_SAMPLE_CSV) {
					for (String s : list) {
						writeCSVtoFile(userId, CSV_Type.Accel, s);
					}
				}
				for (List<String> list : ACC_SAMPLE_CSV)
					list.clear();
				// GPS
				reduceSampleCSV(GPS_SAMPLE_CSV);
				for (List<String> list : GPS_SAMPLE_CSV) {
					for (String s : list) {
						writeCSVtoFile(userId, CSV_Type.GPS, s);
					}
				}
				for (List<String> list : GPS_SAMPLE_CSV)
					list.clear();
			} else if (type == Init_Type.Software) {
				// App
				reduceSampleCSV(APP_SAMPLE_CSV);
				for (List<String> list : APP_SAMPLE_CSV) {
					for (String s : list) {
						writeCSVtoFile(userId, CSV_Type.App, s);
					}
				}
				for (List<String> list : APP_SAMPLE_CSV)
					list.clear();
			}
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
		} else if (type == Init_Type.Software) {
			f = new File(Parameter.SOFTWARE_TRAINING_PATH + userId);
			f.mkdirs();
			f = new File(Parameter.SOFTWARE_TESTING_PATH + userId);
			f.mkdirs();
		}
		// csv folder
		f = new File(Parameter.CSV_OUTPUT);
		f.mkdirs();
	}

	public void initializeData(String targetFileName, String fileName, String userId, FileStats stats, Init_Type type) {
		try {
			initializeData(new FileReader(targetFileName), fileName, userId, stats, type);
		} catch (Exception e) {
			System.err.println("[ERR-Parser.initData]: " + targetFileName + " is broken !");
		}
	}

	private void refreshCSV(String userId, Init_Type type) {
		File f = null;
		if (type == Init_Type.Hardware) {
			// Accel
			if (IS_GENERATING_SAMPLE_FILES)
				f = new File(String.format(ACC_CSV_NAME, Parameter.CSV_OUTPUT, SAMPLE_PREFIX, userId));
			else
				f = new File(String.format(ACC_CSV_NAME, Parameter.CSV_OUTPUT, userId));
			if (f.exists())
				f.delete();
			// GPS
			if (IS_GENERATING_SAMPLE_FILES)
				f = new File(String.format(GPS_CSV_NAME, Parameter.CSV_OUTPUT, SAMPLE_PREFIX, userId));
			else
				f = new File(String.format(GPS_CSV_NAME, Parameter.CSV_OUTPUT, userId));
			if (f.exists())
				f.delete();
		} else if (type == Init_Type.Software) {
			// App
			if (IS_GENERATING_SAMPLE_FILES)
				f = new File(String.format(APP_CSV_NAME, Parameter.CSV_OUTPUT, SAMPLE_PREFIX, userId));
			else
				f = new File(String.format(APP_CSV_NAME, Parameter.CSV_OUTPUT, userId));
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
		if (Parameter.MODE == TrainingMode.Default)
			random(arr.size());
		// TODO add Cross Validation
		// else if (Parameter.MODE == TrainingMode.CrossValidation)
		// crossValidation(arr.size());
		JSONArray trainingArray = new JSONArray();
		JSONArray testingArray = new JSONArray();

		try {
			while (iterator.hasNext()) {
				JSONObject parse = (JSONObject) iterator.next();
				JSONObject unitObj = new JSONObject();
				Iterator<?> tempIterator = null;
				JSONObject temp = null;
				String csvString = null;

				// Get the activity label from data
				String lifelable = (String) parse.get("lifelable");
				String lifelabel = (String) parse.get("lifelabel");
				// Because version 1 use "lifelable"
				if (lifelable != null)
					activity = lifelable;
				else if (lifelabel != null)
					activity = lifelabel;
				unitObj.put("lifelabel", activity);

				int activityIndex = -1;
				for (int i = 0; i < Parameter.getTargetActivities().length; i++) {
					if (Parameter.getTargetActivities()[i].equals(activity)) {
						activityIndex = i;
						break;
					}
				}

				if (type == Init_Type.Hardware) {
					JSONArray accel = (JSONArray) parse.get("Accel");
					JSONArray gps = (JSONArray) parse.get("GPS");
					unitObj.put("Accel", accel);
					unitObj.put("GPS", gps);
					// Accel
					tempIterator = accel.iterator();
					while (tempIterator.hasNext()) {
						temp = (JSONObject) tempIterator.next();
						csvString = buildCSV(userId, activity, temp, CSV_Type.Accel);
						stats.addNumData(1);
						if (IS_GENERATING_SAMPLE_FILES)
							ACC_SAMPLE_CSV.get(activityIndex).add(csvString);
					}
					// GPS
					if (gps.size() > 0) {
						tempIterator = gps.iterator();
						while (tempIterator.hasNext()) {
							temp = (JSONObject) tempIterator.next();
							csvString = buildCSV(userId, activity, temp, CSV_Type.GPS);
							((HardwareStats) stats).addNumGps(1);
							if (IS_GENERATING_SAMPLE_FILES)
								GPS_SAMPLE_CSV.get(activityIndex).add(csvString);
						}
					}
					stats.incrementActivity(activityIndex, accel.size());
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
						csvString = buildCSV(userId, activity, temp, CSV_Type.App, time);
						if (IS_GENERATING_SAMPLE_FILES)
							APP_SAMPLE_CSV.get(activityIndex).add(csvString);
					}
					stats.incrementActivity(activityIndex);
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
//				System.gc();
			} // End of iterator

			if (type == Init_Type.Software) {
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
				else if (type == Init_Type.Software)
					writer = new FileWriter(Parameter.SOFTWARE_TESTING_PATH + fileName);
				writer.write(testingArray.toString());
				writer.close();
			}

		} catch (Exception e) {
			System.err.println("[ERR-Parser.InitData]: " + e.getMessage());
		}
	}

	private void reduceSampleCSV(List<List<String>> sample) {
		Random r = new Random();
		// For each activity, the minimum sample
		int min = SAMPLE_NUMBER;
		for (List<?> list : sample) {
			if (list.size() < min && list.size() > 0)
				min = list.size();
		}
		for (int i = 0; i < sample.size(); i++) {
			List<String> list = sample.get(i);
			List<String> newList = new ArrayList<>();
			if (list.size() > 0) {
				while (newList.size() < min) {
					newList.add((String) list.remove(r.nextInt(list.size())));
				}
				list.clear();
				list.addAll(newList);
			}
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
			sb.append(parse.get("Y")).append(','); // latitude
			sb.append(parse.get("X")).append(','); // longitude
			sb.append(parse.get("Speed"));
		} else if (type == CSV_Type.App) {
			sb.append(parse.get("name"));
		}
		if (others.length > 0) {
			for (String s : others) {
				sb.append(',').append(s);
			}
		}
		sb.append('\n');
		if (Parameter.IS_GENERATING_CSV_FILES) {
			writeCSVtoFile(userId, type, sb.toString());
		}
		return sb.toString();
	}

	private void writeCSVtoFile(String userId, CSV_Type type, String s) {
		Writer writer = null;
		try {
			String format = null;
			File f = null;
			if (type == CSV_Type.Accel)
				format = ACC_CSV_NAME;
			else if (type == CSV_Type.GPS)
				format = GPS_CSV_NAME;
			else if (type == CSV_Type.App)
				format = APP_CSV_NAME;
			if (IS_GENERATING_SAMPLE_FILES)
				f = new File(String.format(format, Parameter.CSV_OUTPUT, SAMPLE_PREFIX, userId));
			else
				f = new File(String.format(format, Parameter.CSV_OUTPUT, userId));
			writer = new FileWriter(f, true);
			writer.append(s);
			writer.close();
		} catch (IOException e) {
			System.err.println("[ERR-Parser.writeCSV]: " + e.getMessage());
		}
	}
}
