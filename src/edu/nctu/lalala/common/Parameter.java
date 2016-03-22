package edu.nctu.lalala.common;

import edu.nctu.lalala.enums.TrainingMode;

public class Parameter {
	// Folder type
	static String[] FOLDER_TYPE = { "random", "cross", "learning" };
	// User defined parameter
	public static boolean IS_DEBUG = true;
	public static boolean IS_GENERATING_TRAINING_FILES = false;
	public static boolean IS_GENERATING_TESTING_FILES = false;
	public static boolean IS_GENERATING_CSV_FILES = false;
	// Training mode
	public static TrainingMode MODE = TrainingMode.Default;
	// Dataset base directory
	static String BASE_FOLDER = "A:\\_Download\\Lai\\Dataset\\3D-AIM\\";
	static String WORKING_FOLDER = BASE_FOLDER + Parameter.getWorkingDirectory() + "\\";
	// Active users in this experiments
	static String[] USERS = {
			// Top 10 users
//			"353567051351832", "353567051352475", "353567051353648", "353567051354810", "353567051354901",
//			"353567051355254", "355027051966921", "355027054515550", "355387051404228", "864690022758703",
			// Other
//			"353567051351956", "355859056961972", "356440047806292", "863360029004219", "352241062477314",
//			"354855061711965", "356063057559765", "865312025215222",
			// Only 1 activity
//			"353567051354828", "353567051354935", "354435052172343", "354833059525127", "863985028111613"
			};

	// Paths
	public static String SOFTWARE_PATH = WORKING_FOLDER + "Software_Original\\";
	public static String HARDWARE_PATH = WORKING_FOLDER + "Hardware_Original\\";

	public static String SOFTWARE_TRAINING_PATH = WORKING_FOLDER + "Software_Training\\";
	public static String HARDWARE_TRAINING_PATH = WORKING_FOLDER + "Hardware_Training\\";

	public static String SOFTWARE_TESTING_PATH = WORKING_FOLDER + "Software_Testing\\";
	public static String HARDWARE_TESTING_PATH = WORKING_FOLDER + "Hardware_Testing\\";

	// For case studies
	public static String CSV_OUTPUT = WORKING_FOLDER + "CSV\\";

	// Activities in our experiments
	private static String[] TARGET = { "Working", "Dining", "Transportation", "Sporting", "Shopping", "Entertainment" };

	// Training ratio (0.8 means 80% for training and 20% for testing)
	static double training = 0.8;

	// For 5-fold cross validation
	static int crossValidationFold = 5;

	/* *** helper functions *** */
	public static double getTrainingRatio() {
		return training;
	}

	public static double getCrossValidationCeiling() {
		return 1.0;
	}

	public static double getCrossValidationFloor() {
		double d = (double) 1.0 / crossValidationFold;
		return getCrossValidationCeiling() - d;
	}

	public static String[] getUsers() {
		return USERS;
	}

	public static String getWorkingDirectory() {
		switch (MODE) {
		case CrossValidation:
			return FOLDER_TYPE[1];
		case Default:
			return FOLDER_TYPE[0];
		case LearningCurve:
			return FOLDER_TYPE[2];
		default:
			return FOLDER_TYPE[0];
		}
	}

	public static String[] getTargetActivities() {
		return TARGET;
	}
	/* *** End of helper function *** */
}
