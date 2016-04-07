package main;
import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class Main {

	// To run from console: mvn exec:java -Dexec.args="-i $inputDataset -o $outputDir"
	public static void main(String[] args) throws Exception {
		// SET IsTrainMode
		Config.isTrainMode = !(new File(Config.TRAIN_FILE_PATH).exists());
		
		// SET FOLDERS
		Config.setFolders(args);

		// CLEAR OUTPUT FOLDER
		File outputFolder = new File(Config.outputFolderPath);
		FileUtils.cleanDirectory(outputFolder);
		
		// SETUP INPUT
		File inputFolder = new File(Config.inputFolderPath);
		File inputInfoJson = new File(inputFolder, "info.json");
		
		Type jsonProblemListType = new TypeToken<ArrayList<JsonProblem>>() {}.getType();
		List<JsonProblem> jsonProblems = new Gson().fromJson(FileUtils.readFileToString(inputInfoJson), jsonProblemListType);
		
		for(JsonProblem problem : jsonProblems) {
			String folderName = problem.getFolder();
			
			FeaturesGenerator.generateFeaturesSimilarities(inputFolder, folderName);
			
			if(Config.isTrainMode) {
				FeaturesGenerator.setActualSimilarities(folderName);
			}
		}
		
		// TRAIN
		FeaturesGenerator.trainResults();
		
		// GENERATE RESULTS
		for(JsonProblem problem : jsonProblems) {
			String folderName = problem.getFolder();
			
			Results.generateResults(folderName);
			
			Results.generateOutput(new File(outputFolder, folderName));
		}
		
		// CALCULATE ERROR ONLY IF TRAIN MODE
		if(Config.isTrainMode) {
			Results.calculateError();
		}
	}

}
