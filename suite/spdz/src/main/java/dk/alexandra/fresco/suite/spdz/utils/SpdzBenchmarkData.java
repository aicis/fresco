package dk.alexandra.fresco.suite.spdz.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class SpdzBenchmarkData {
	
	private static boolean testing = false; //true to disable logging of multiple players. 
	private static boolean isLogging = true; //True to enable logging of timings
	private static boolean showIndention = true; //True to enable indention in the log file
	private static boolean isInitialized = false;
	private static int currentId;
	private static long startTime;
	private static HashMap<String, Long> startedFunctions;
	private static FileWriter writer;
	private static StringBuffer sb;
	private static final String indentChar = ">";
	
	private static int triplesUsed;
	private static int bitsUsed;
	private static int exponentiationPipesUsed;
	private static int inputsUsed;
	private static int squaresUsed;
	
	private static void startLogger() {
		
		startedFunctions = new HashMap<String, Long>();
		sb = new StringBuffer();
		sb.append("ID;Function;Time(ms);Starting/stopping;Duration(ms)\n");

		currentId = 0;
		startTime = System.nanoTime();
		isLogging = true;
		isInitialized = true;
	}
	
	public static void stopLogger() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
		String folderName = "timings";
		String fileName = folderName+"/Timings " + dateFormat.format(new Date()) + ".csv";
		
		File folder = new File(folderName);
		if(!folder.exists()){
			folder.mkdir();
		}
		try {
			writer = new FileWriter(fileName);
			writer.write(getString());
			writer.close();
		} catch (IOException e) {
			System.out.println("Failed to close timings log: " + e);
		}
		isLogging = false;		
	}
	
	public static void enableTestMode(){
		testing = true;
	}
	
	/**
	 * Toggle the logger on/off.
	 * 
	 * @return The new state of the logger.
	 */
	public static boolean toogleLogger() {
		if (!isLogging) {
			//Toggle on
			startLogger();
		} else {
			//Toggle off
			stopLogger();
		}
		return isLogging;
	}
	
	public static void resetTimer() {
		startTime = System.nanoTime();
	}
	
	/**
	 * When called, this method saves a line in the timings log showing the
	 * given function and the duration since the logger was started (or
	 * resetTimer was called). When called to indicate that a function is
	 * stopping (starting = false), the duration since the corresponding start
	 * is also logged. Recursion cannot be handled since the function name has
	 * to be unique.
	 * 
	 * @param function
	 * @param starting
	 */
	public static void logTiming(String function, boolean starting, int myID) {
		if(testing && myID != 1){
			return; //prevent multiple players from logging. Only one on test machines (localhost)
		}
		if (!isLogging)
			return;
		
		if (!isInitialized)
			startLogger();
		
		long time = (System.nanoTime() - startTime) / 1000000;
		String line = function + ";" + time;
		
		if (starting) {
			startedFunctions.put(function, new Long(time));
			line += ";start;\n";
		} else {
			if (startedFunctions.containsKey(function)) {
				long duration = time - startedFunctions.get(function);
				startedFunctions.remove(function);
				line += ";stop;" + duration + "\n";
				System.out.println(">> Timing " + function + ": " + duration + " ");
			} else {
				line += ";stop;n/a\n";
			}
		}

		String indention;
		if (showIndention) {
			if (starting)
				indention = getIndentionPrefix(startedFunctions.size() - 1);
			else
				indention = getIndentionPrefix(startedFunctions.size());
		} else
			indention = "";

		sb.append(currentId + ";" + indention + line);


		currentId++;
	}
	
	private static String getIndentionPrefix(int depth) {		
		String indent = "";
		int i;
		for (i=0; i<depth; i++)
			indent += indentChar;
		return indent;
	}	
	
	public static void tripleUsed(int myID){
		if(testing && myID != 1){
			return;
		}
		triplesUsed++;
	}
	
	public static int getTriplesUsed(){
		return triplesUsed;
	}
	
	public static void bitUsed(int myID){
		if(testing && myID != 1){
			return;
		}
		bitsUsed++;
	}
	
	public static int getbitsUsed(){
		return bitsUsed;
	}
	
	public static void exponentiationPipeUsed(int myID){
		if(testing && myID != 1){
			return;
		}
		exponentiationPipesUsed++;
	}
	
	public static int getexponentiationPipesUsed(){
		return exponentiationPipesUsed;
	}
	
	public static void inputUsed(int myID){
		if(testing && myID != 1){
			return;
		}
		inputsUsed++;
	}
	
	public static int getinputsUsed(){
		return inputsUsed;
	}
	
	public static void squareUsed(int myID){
		if(testing && myID != 1){
			return;
		}
		squaresUsed++;
	}
	
	public static int getsquaresUsed(){
		return squaresUsed;
	}
	
	public static String getString() {
		StringBuilder builder = new StringBuilder();
		builder.append("==================== Benchmarks ====================");
		builder.append('\n');
		builder.append("Triples used: "+triplesUsed);
		builder.append('\n');
		builder.append("Bits used: "+bitsUsed);
		builder.append('\n');
		builder.append("Exponentiation pipes used: "+exponentiationPipesUsed);
		builder.append('\n');
		builder.append("Inputs used: "+inputsUsed);
		builder.append('\n');
		builder.append("Squares used: "+squaresUsed);
		builder.append('\n');
		builder.append("\n========== Timings =========");
		builder.append('\n');
		builder.append(sb.toString());
		builder.append('\n');
		builder.append("========== Timings =========");
		builder.append('\n');
		builder.append("==================== Benchmarks ====================");
		builder.append('\n');
		return builder.toString();
	}

	public static void printBenchmark(int myID) {
		if(testing && myID != 1){
			return;
		}		
		System.out.println(getString());
	}
	
}
