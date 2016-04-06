import java.util.List;

import features.*;

public class Main {

	@SuppressWarnings("unused")
	public static void main(String[] args) throws Exception {
		String folderName = "problem001";
		
		FeaturesGenerator.generateFeaturesSimilarities(folderName);
		
		Results.generateResults();
		
		List<DocumentsSimilarity> trainSimilarities = FeaturesGenerator.getActualSimilarities(folderName);
	}

}
