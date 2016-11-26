package utils;

import java.io.File;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import utils.Utils;

public class FileCreator {

	/**
	 * Creates a file, includes some things for testing. Afterwards, the created file can
	 * be found in the outputFile
	 * @param inputFile the file to be added with the needed inputs (and asserts and assumes later on)
	 * @param outputFile the file that is able to be parsed by cbmc
	 * @throws IOException
	 */
	public boolean createFile(String inputFile, String outputFile) throws IOException {
		// URL url = this.getClass().getClassLoader().getResource("/res");

		File file = new File("src/res/" + inputFile);
		File outFile = new File("src/res/" + outputFile);

		List<String> code = Utils.readListFromFile(file.toPath());

		int lastPre = 0;

		//search for index of the latest occuring include
		for (int i = 0; i < code.size(); i++) {
			if (code.get(i).contains("#include <")) {
				lastPre = i;
			}
		}
		
		code.add(lastPre + 1, "#include <assert.h>");
		
		//search for index of the latest occuring defindes
		for (int i = 0; i < code.size(); i++) {
			if (code.get(i).contains("#define")) {
				lastPre = i;
			}
		}
		
		code.add(lastPre + 1, "#define assert2(x, y) __CPROVER_assert(x, y)");
		code.add(lastPre + 2, "#define assume(x) __CPROVER_assume(x)");
		
		
		//here you could add asserts and assumes in the code that is about to be checked
		
		Utils.WriteStringLinesToFile(Utils.stringListToArr(code), file.getParentFile().getAbsolutePath(), outputFile);
		
		return Files.exists(outFile.toPath());
	}

}
