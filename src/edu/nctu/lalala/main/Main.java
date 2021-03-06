package edu.nctu.lalala.main;

import java.util.Date;

import edu.nctu.lalala.parser.Parser;

// The main class for all
public class Main {

	public static void main(String[] args) {
		System.out.println("~~~~~ Program started ~~~~~~ " + new Date());
		GenerateTrainingTestingFiles();
		System.out.println("~~~~~ Program finished ~~~~~ " + new Date());
	}
	
	public static void GenerateTrainingTestingFiles()
	{
		long time = System.nanoTime();
		Parser p = new Parser();
		p.processAllUsers();
		long duration = (System.nanoTime() - time)/1000/1000/1000;
		System.out.println(String.format("Finishing training testing file generation in: %d seconds (%d minutes)", duration, duration/60));
	}

}
