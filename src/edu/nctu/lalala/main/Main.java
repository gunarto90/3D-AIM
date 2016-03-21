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
		Parser p = new Parser();
		p.ProcessAllUsers();
	}

}
