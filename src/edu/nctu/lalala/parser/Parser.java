package edu.nctu.lalala.parser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
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

public class Parser {

	List<Integer> trainingIndex = new ArrayList<>();

	public void ProcessAllUsers() {
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

//		System.out.println(HardwareFile.getAbsolutePath());
		if (HardwareFile.isDirectory()) // Just to make sure no error happens
		{
			String[] s = HardwareFile.list(); // List
//			System.out.println("size : " + s.length);
			for (int i = 0; i < s.length; i++) {
				HardwareFileList.add(s[i]);
			}
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
			InitializeHardware(hardwareFileName, filename, stats);
		}

		return stats;
	}

	public void InitializeHardware(String hardwareFileName, String fileName, FileStats stats) {
		try {
			InitializeHardware(new FileReader(hardwareFileName), fileName, stats);
		} catch (Exception e) {
			System.err.println("Hardware:" + hardwareFileName + " is broken !");
		}
	}

	public void InitializeHardware(Reader hardwareReader, String fileName, FileStats stats)
			throws IOException, ParseException {
		JSONParser jsonParser = new JSONParser();
		JSONArray arr = (JSONArray) jsonParser.parse(hardwareReader);
		String activity = null;
		Iterator<?> iterator = arr.iterator();
		int iteratorCounter = 0;

		// System.out.println("JSON unit: "+arr.size());
		random(arr.size());
		stats.addNumData(arr.size());
		// crossValidation(arr.size());
		JSONArray trainingMainArray = new JSONArray();
		JSONArray testingMainArray = new JSONArray();
		try {
			while (iterator.hasNext()) {
				JSONObject parse = (JSONObject) iterator.next();

				JSONArray accel = (JSONArray) parse.get("Accel");
				String lifelable = (String) parse.get("lifelable");
				String lifelabel = (String) parse.get("lifelabel");
				JSONArray gps = (JSONArray) parse.get("GPS");

				if (gps.size() > 0)
					stats.incrementNumGps(); // global gps point counter
				// Because version 1 use "lifelable"
				if (lifelable != null)
					activity = lifelable;
				else if (lifelabel != null)
					activity = lifelabel;

				JSONObject unitObj = new JSONObject();
				unitObj.put("Accel", accel);
				unitObj.put("GPS", gps);
				unitObj.put("lifelabel", activity);

				// Match index
				for (int i = 0; i < Parameter.getTargetActivities().length; i++) {
					if (Parameter.getTargetActivities()[i].equals(activity)) {
						stats.incrementActivity(i);
						// System.out.println(i + ":" + activity);
						break;
					}
				}
				// random
				if (trainingIndex.contains(iteratorCounter))
					trainingMainArray.add(unitObj);
				else
					testingMainArray.add(unitObj);
				// cross validation
				// if(trainingIndex.contains(iteratorCounter))
				// testingMainArray.add(unitObj);
				// else
				// trainingMainArray.add(unitObj);

				iteratorCounter++;
			}
			// Write to file
			// System.out.println(parameter.HardwareTraining_path+fileName);
			// Writer writer = new
			// FileWriter(parameter.HardwareTraining_path+fileName);
			// writer.write(trainingMainArray.toString());
			// writer.close();
			//
			// writer = new FileWriter(parameter.HardwareTesting_path+fileName);
			// writer.write(testingMainArray.toString());
			// writer.close();
		} catch (Exception e) {
			System.err.println("????????????????????????????????????????" + e.getMessage());
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
		Iterator<Integer> iterator = trainingIndex.iterator();
	}
}
