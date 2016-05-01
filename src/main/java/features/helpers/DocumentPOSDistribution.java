package features.helpers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.lucene.wordnet.AnalyzerUtil;

import main.Config;

import org.apache.commons.io.FileUtils;

import nlp.pos.AbstractPOSTagger;

import nlp.pos.POSTaggerFactory;

public class DocumentPOSDistribution extends Distribution {
	private File document;
	private File postagCountDocument;
	private String language;

	private TreeMap<String, LinkedList<Integer>> postagDistributions = new TreeMap<String, LinkedList<Integer>>();

	public DocumentPOSDistribution(File document, String language) {
		if (document == null || !document.exists()) {
			throw new RuntimeException("File not found.");
		}
		if (language == null || language.trim().isEmpty()) {
			throw new RuntimeException("Language not specified.");
		}
		this.postagDistributions = new TreeMap<String, LinkedList<Integer>>();
		this.document = document;
		File postagDir = new File(document.getParent() + "/postag/");
		if (!postagDir.exists()){
			postagDir.mkdir();
		}
		this.postagCountDocument = new File(document.getParent() + "/postag/" + document.getName());
		if (!this.postagCountDocument.exists()) {
			try {
				persistPOStagOccurencesDistribution(FileUtils.readFileToString(document), language,
						this.postagCountDocument);

			} catch (IOException e) {
				e.printStackTrace();
			}
			// If the distribution is already generated
		} else {
			//TODO implement !
		}
	}

	protected void persistPOStagOccurencesDistribution(String text, String language, File outputFile) {
		AbstractPOSTagger posTagger = POSTaggerFactory.get(language);
		String[] sentences = AnalyzerUtil.getSentences(text, 0);

		for (int sentenceIndex = 0; sentenceIndex < sentences.length; sentenceIndex++) {
			if (sentences[sentenceIndex] == null || sentences[sentenceIndex].trim().isEmpty()) {
				continue;
			}
			List<POSTagEntry> tags = posTagger.tag(sentences[sentenceIndex]);
			// get all entries in order to retrieve the number of
			// occurrences of all postags
			for (POSTagEntry entry : tags) {
				if (entry.getWord() == null || entry.getWord().trim().isEmpty()) {
					continue;
				}
				// check if this is the first occurrence of the postag
				LinkedList<Integer> list = null;
				if (entry.getPostag() == null) {
					continue;
				}
				if (!this.postagDistributions.containsKey(entry.getPostag())) {
					list = new LinkedList<Integer>();
					// put zero number of occurrences for all previous
					// sentences
					while (list.size() < sentenceIndex) {
						list.add(0);
					}
					list.add(new Integer(1));
					this.postagDistributions.put(entry.getPostag(), list);
				} else {
					list = this.postagDistributions.get(entry.getPostag());
					// add one more occurrence in the current sentence
					// list.set(list.get(index),
					// Integer.sum(list.get(list.size() - 1), 1));
					// System.out.println(entry.getPostag());
					while (list.size() < sentenceIndex) {
						list.add(0);
					}
					list.set(list.size() - 1, Integer.sum(list.getLast(), 1));
				}
			}
		}
		persistPOStagOccurencesDistribution(postagDistributions, outputFile);
	}

	private void persistPOStagOccurencesDistribution(TreeMap<String, LinkedList<Integer>> postagDistributions,
			File outputFile) {
		if (postagDistributions == null || postagDistributions.size() == 0) {
			System.err.println("Empty map.");
			return;
		}
		if (outputFile == null) {
			System.err.println("File not found.");
			return;
		}
		// This is where the file goes.

		// This line isn't needed, but is really useful
		// if you're a beginner and don't know where your file is going to end
		// up.
		System.out.println("Persisting postag distributions in " + outputFile.getAbsolutePath());
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(outputFile));
			outputFile.createNewFile();
			for (Map.Entry<String, LinkedList<Integer>> entry : postagDistributions.entrySet()) {
				StringBuilder entrySB = new StringBuilder();
				entrySB.append(entry.getKey());
				for (Integer numOfOccurences : entry.getValue()) {
					entrySB.append("," + numOfOccurences);
				}
				writer.write(entrySB.toString());
				writer.newLine();
			}
			writer.flush();
		} catch (IOException e) {
			// Useful error handling here
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private void setPostagDistributions(File file) {
		try {
			List<String> lines = Files.readAllLines(Paths.get(file.getPath()));
			for (String line : lines) {
				String[] elements = line.split(",");
				String postag = elements[0];
				LinkedList<Integer> linkedList = new LinkedList<Integer>();
				for (int i = 1; i < elements.length; i++) {
					linkedList.add(Integer.getInteger(elements[i]));
				}
				this.postagDistributions.put(postag, linkedList);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// LinkedHashMap<String, String> greekTags = greekTagger.tag(text);
	// for (Map.Entry<String, String> greekEntry : greekTags.entrySet()) {
	// System.out.println(greekEntry.getKey() + " -> " + greekEntry.getValue());
	// }
	// TODO return copy of the object in order to avoid external and possibly
	// malicious changes
	public TreeMap<String, LinkedList<Integer>> getPOStagDistributions() {
		
		return postagDistributions;
	}
}
