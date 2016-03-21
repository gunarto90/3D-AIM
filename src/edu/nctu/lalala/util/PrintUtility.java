package edu.nctu.lalala.util;

public class PrintUtility {
	private PrintUtility() {
	}

	private static final PrintUtility singleton = new PrintUtility();

	public static PrintUtility getInstance() {
		return singleton;
	}
	
	public String printArray(int... arr)
	{
		StringBuilder sb =new StringBuilder();
		sb.append('[');
		for(int i=0; i<arr.length; i++)
		{
			sb.append(arr[i]);
			if(i<arr.length-1)
				sb.append(", ");
		}
		sb.append(']');
		return sb.toString();
	}
	
	public String printArray(double... arr)
	{
		StringBuilder sb =new StringBuilder();
		sb.append('[');
		for(int i=0; i<arr.length; i++)
		{
			sb.append(arr[i]);
			if(i<arr.length-1)
				sb.append(", ");
		}
		sb.append(']');
		return sb.toString();
	}
	
	public String printArray(char... arr)
	{
		StringBuilder sb =new StringBuilder();
		sb.append('[');
		for(int i=0; i<arr.length; i++)
		{
			sb.append(arr[i]);
			if(i<arr.length-1)
				sb.append(", ");
		}
		sb.append(']');
		return sb.toString();
	}
	
	public String printArray(String... arr)
	{
		StringBuilder sb =new StringBuilder();
		sb.append('[');
		for(int i=0; i<arr.length; i++)
		{
			sb.append(arr[i]);
			if(i<arr.length-1)
				sb.append(", ");
		}
		sb.append(']');
		return sb.toString();
	}
}
