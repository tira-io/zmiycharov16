package features;

import featureHelpers.IdentificationDocument;
import featureHelpers.Feature;

public class MeanSentenceLength extends Feature {

	public MeanSentenceLength() {
		super("MeanSentenceLengthFeature");
	}

	@Override
	public double getSimilarity(IdentificationDocument doc1, IdentificationDocument doc2) {
		double mslDoc1 = doc1.getMeanSentenceLength();
		double mslDoc2 = doc2.getMeanSentenceLength();

		double result = mslDoc1 > mslDoc2 ? (mslDoc2 / mslDoc1) : (mslDoc1 / mslDoc2);
		return result;
	}
}
