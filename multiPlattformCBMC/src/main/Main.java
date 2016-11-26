package main;

import java.io.IOException;
import java.util.List;

import cmbcInterface.CallCbmc;
import utils.FileCreator;

/**
 * 
 * @author Lukas
 *
 */
public class Main {
	//variable that indicates that the program was ended prematurely 
	private static boolean isInterrupted = false;
	
	//the name for the file that is to be checked.
	//IMPORTANT: so far the files to be checked and to be parsed should
	//lay under /src/res/ 
	private static String fileName = "temp_file_for_checking.c";

	public static void main(String[] args) throws IOException {
		FileCreator fc = new FileCreator();

		fc.createFile("testProgram.c", fileName);

		List<String> output = CallCbmc.callCbmc(fileName, 0,
				"--trace" , "--unwind 10");

		if (!isInterrupted) {
			output.forEach(in -> {
				System.out.println(in);
			});
		}
	}
	
	/**
	 * indicate to the Main program, that the 
	 * execution of cbmc was stopped forcefully
	 */
	public static void interrupt() {
		isInterrupted = false;
	}
}
