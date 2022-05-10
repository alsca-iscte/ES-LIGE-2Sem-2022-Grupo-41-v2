package org.biojava.nbio.core.sequence.io;


import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class IUPACTableProduct {
	private final String baseOne;
	private final String baseTwo;
	private final String baseThree;

	public IUPACTableProduct(String baseOne, String baseTwo, String baseThree) {
		this.baseOne = baseOne;
		this.baseTwo = baseTwo;
		this.baseThree = baseThree;
	}

	public List<List<String>> codonStrings() {
		List<List<String>> codons = new ArrayList<List<String>>();
		for (int i = 0; i < baseOne.length(); i++) {
			List<String> codon = Arrays.asList(Character.toString(baseOne.charAt(i)),
					Character.toString(baseTwo.charAt(i)), Character.toString(baseThree.charAt(i)));
			codons.add(codon);
		}
		return codons;
	}
}