package cmbcInterface;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import main.Main;
import utils.Interrupter;
import utils.ThreadedBufferedReader;
import utils.Utils;

public class CallCbmc {

	private static boolean isLinux = false;
	private static boolean isWindows = false;
	//falls man einen timeout will, soll der mindestens 10 Sekunden betragen
	private static long minSleepTime = 10000;
	//amount of 1 second waits to be done while waiting for the process to shut down.
	private static int maxWaits = 20;

	/**
	 * This function calls cbmc. First of all it tries to determine the OS so it
	 * can call it in a specific manner for the OS.
	 * 
	 * Then it attaches two readers to the Program and waits for the program to
	 * finish. If it is wished, a timeout can also be added
	 * 
	 * @param fileName
	 *            the name of the file to be checked
	 * @param parameters
	 *            the additional parameters to be given to cbmc
	 * @param interruptTime
	 *            The time in milliseconds after which the program should
	 *            interrupt itself. If it is smaller or equal to there there
	 *            will be no interrupt
	 * @return The List that contains the normal Output of the program, line by
	 *         line
	 * @throws IOException
	 */
	public static ArrayList<String> callCbmc(String fileName, long interruptTime, String... parameters)
			throws IOException {

		ProcessBuilder prossBuild = null;
		Process cbmcProcess = null;

		Interrupter interrupter = null;

		ArrayList<String> result = new ArrayList<>();
		ArrayList<String> errors = new ArrayList<>();

		ThreadedBufferedReader outReader;
		ThreadedBufferedReader errReader;
		// get the path where the file that is to be checked is located
		String filePath = Utils.getFileFromRes(fileName);

		CountDownLatch latch = new CountDownLatch(2);
		
		// get the operation system
		String os = System.getProperty("os.name");

		isWindows = (os.toLowerCase().contains("windows"));
		isLinux = (os.toLowerCase().contains("linux"));

		if (isLinux) {
			String arguments = "";
			// add up the arguments for the call
			for (int i = 0; i < parameters.length; i++) {
				// Linux doesn't need the trace command. So we don't want it
				// here
				if (!parameters[i].contains("trace")) {
					arguments = arguments + " " + parameters[i];
				}
			}

			// TODO: get the arguments to work on linux. Maybe they have to be
			// in an other position?
			// prossBuild = new ProcessBuilder("cbmc", filePath, arguments);

			prossBuild = new ProcessBuilder("cbmc", filePath, arguments);

		} else if (isWindows) {
			String vsCmd = Utils.getVScmdPath();

			if (vsCmd == null) {
				Main.interrupt();
				return result;
			}

			String cbmcEXE = Utils.getFileFromRes("/cbmcWIN/cbmc.exe");

			// assemble the call that is to be given to the process
			String cbmcCall = "\"" + vsCmd + "\"" + " & " + cbmcEXE + " " + filePath;

			// add all the other missing arguments for that call
			for (int i = 0; i < parameters.length; i++) {
				cbmcCall = cbmcCall + " " + parameters[i];
			}

			prossBuild = new ProcessBuilder("cmd.exe", "/c", cbmcCall);

		} else {
			System.err.println("You are using an unknown OS. Shutting down.");
			System.exit(0);
		}

		try {
			cbmcProcess = prossBuild.start();

		} catch (IOException e) {
			e.printStackTrace();
		}

		if (cbmcProcess != null) {
			
			//set up the readers and start them
			outReader = new ThreadedBufferedReader(
					new BufferedReader(new InputStreamReader(cbmcProcess.getInputStream())), result, latch);
			errReader = new ThreadedBufferedReader(
					new BufferedReader(new InputStreamReader(cbmcProcess.getErrorStream())), errors, latch);

			outReader.start();
			errReader.start();

			// check if an interrupt is wished
			if (interruptTime > 0) {
				interruptTime = Math.max(interruptTime, minSleepTime);
				interrupter = new Interrupter(Thread.currentThread(), interruptTime);
				interrupter.start();
			}

			try {
				cbmcProcess.waitFor();
				//the process is finished now
			} catch (InterruptedException e) {
				//kill the process
				killProcess(cbmcProcess);
				outReader.interrupt();
				errReader.interrupt();
				//the process was interrupted, so we can't give an output
				Main.interrupt();
			}
			
			try {
				latch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if(interrupter != null) {
				interrupter.disable();
				//shut down the Thread
				interrupter.interrupt();
			}

			/*
			 * For debug purposes it can be beneficial when the file you want to
			 * work with is still there. Therefore I leave this commented out
			 * for now.
			 */

			// Utils.deleteFile(fileName);

		} else {
			// process wasn't able to start, so there is nothing to be printed
			Main.interrupt();
		}
		return result;
	}

	public static boolean killProcess(Process toKill) {
		if (!toKill.isAlive()) {
			System.err.println("Warning, process isn't alive anymore");
			return false;
		} else if (isLinux) {
			// closing on Linux is nice and clean (at least on Ubuntu)
			toKill.destroyForcibly();
		} else if (isWindows) {
			try {
				// with this you can turn off cbmc in windows. it is a horrible
				// hack, but i haven't found a working improvement so far for
				// windows
				// This closes ALL CBMC instances, so keep that in mind.
				Runtime.getRuntime().exec("taskkill /F /IM cbmc.exe");
				//because the process wont shut down immediately we give it some time.
				for (int i = 0; i < maxWaits ; i++) {
					try {
						if (!toKill.isAlive()) {
							//if it is shut down prematurely we will advance
							break;
						}
						//sleep in 1 second intervals
						Thread.sleep(1000);
						System.out.println("habe geschlafen");
					} catch (InterruptedException e) {
						System.err.println("hier sollte kein interrupt passieren");
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} catch (IOException e) {
				System.out.println("nochmal interrupted, was nicht sein sollte");
				e.printStackTrace();
			}
		} else {
			return false;
		}

		if (toKill.isAlive()) {
			System.err.println("Warning, it was not possible to terminate the process. Please do it by hand!");
			return false;
		} else {
			Main.interrupt();
			return true;
		}

	}
}
