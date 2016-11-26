/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.io.File;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

/**
 *
 * @author boss
 */
public class Utils {
    public static String[] stringListToArr(List<String> list) {
        String[] arr = new String[list.size()];
        for(int i = 0; i < list.size(); ++i) {
            arr[i] = list.get(i);
        }
        return arr;
    }
    
     public static List<String> stringArrToList(String[] arr) {
        List<String> list = new ArrayList<String>();
        for(String s : arr) {
            list.add(s);
        }
        return list;
    }
    
    public static <E> List<E> deepCopyList(List<E> l) {
        List<E> createdList = new ArrayList<E>(l.size());
        for(E curr : l) {
            createdList.add(curr);
        }
        return createdList;
    }
   
    public static void WriteStringLinesToFile(String[] text, String path, String fileName) 
            throws FileNotFoundException, UnsupportedEncodingException {        
        PrintWriter writer = new PrintWriter(path + '/' + fileName, "UTF-8");
        for(String s : text) {
            writer.println(s);
        }
        writer.close();
    }
    
    public static void writeStringToFile(String text, String path, String fileName) 
            throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter writer = new PrintWriter(path + '/' + fileName, "UTF-8");
        writer.print(text);
        writer.close();
    }

	public static List<String> readListFromFile(Path path) 
			throws IOException {
		
		Files.readAllLines(path);
		return Files.readAllLines(path, Charset.defaultCharset());
	}
	
	public static void deleteFile(String toDelete) throws IOException {
		File file = new File("src/res/" + toDelete);
		Files.delete(file.toPath());
	}
	
	public static String getFileFromRes(String fileName) {
		return new File("src/res/" + fileName).getAbsolutePath();
	}
	
	/**
	 * 
	 * @return 
	 * @throws IOException
	 */
	public static String getVScmdPath() throws IOException {
		// TODO: this could be cached, because it takes a significant time on
		// Windows every startup
		Path x86 = new File("C:/Program Files (x86)").toPath();
		Path x64 = new File("C:/Program Files").toPath();
		String searchTerm = "Microsoft Visual Studio";
		String pathToBatch = "/Common7/Tools/VsDevCmd.bat";

		ArrayList<String> toSearch = new ArrayList<>();
		Files.list(x86).filter(Files::isReadable).filter(path -> path.toString().contains(searchTerm))
				.forEach(VSPath -> toSearch.add(VSPath.toString()));
		Files.list(x64).filter(Files::isReadable).filter(path -> path.toString().contains(searchTerm))
				.forEach(VSPath -> toSearch.add(VSPath.toString()));

		for (Iterator<String> iterator = toSearch.iterator(); iterator.hasNext();) {
			String toCheck = ((String) iterator.next()) + pathToBatch;

			if (Files.isReadable(new File(toCheck).toPath())) {
				return toCheck;
			}
		}

		String userInput = JOptionPane
				.showInputDialog("The progam was unable to find a Developer Command Prompt for Visual Studio. \n"
						+ " Please search for it on your own and paste the path to the batch-file here!");

		// important that the check against null is done first, so invalid
		// inputs are caught without causing an error
		if (userInput != null && Files.isReadable(new File(userInput).toPath()) && userInput.contains("VsDevCmd.bat")) {
			return userInput;
		} else {
			System.err.println("The provided path did not lead to the command prompt. Shutting down now.");
			return null;
		}
	}
	 
}
