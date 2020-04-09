package hu.u_szeged.cons;

import java.util.Map;

import ims.productParser.ProductParserData;

class PPReplaceModel extends ProductParserData {
	
	private static final long serialVersionUID = -3268918617190943304L;
	
	Map<String, Integer> wordFreqs;
	int threshold;

	void setWordFreqs(Map<String, Integer> wordFreqs) {
		this.wordFreqs = wordFreqs;
	}

	void setThreshold(int threshold) {
		this.threshold = threshold;
	}
	
}
