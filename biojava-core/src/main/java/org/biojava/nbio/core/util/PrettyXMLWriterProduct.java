package org.biojava.nbio.core.util;


import java.io.IOException;
import java.io.PrintWriter;

public class PrettyXMLWriterProduct {
	public void printChars(String data, PrintWriter thisWriter) throws IOException {
		if (data == null) {
			printChars("null", thisWriter);
			return;
		}
		for (int pos = 0; pos < data.length(); ++pos) {
			char c = data.charAt(pos);
			if (c == '<' || c == '>' || c == '&') {
				numericalEntity(c, thisWriter);
			} else {
				thisWriter.write(c);
			}
		}
	}

	public void numericalEntity(char c, PrintWriter thisWriter) throws IOException {
		thisWriter.print("&#");
		thisWriter.print((int) c);
		thisWriter.print(';');
	}

	public void printAttributeValue(String data, PrintWriter thisWriter) throws IOException {
		if (data == null) {
			printAttributeValue("null", thisWriter);
			return;
		}
		for (int pos = 0; pos < data.length(); ++pos) {
			char c = data.charAt(pos);
			if (c == '<' || c == '>' || c == '&' || c == '"') {
				numericalEntity(c, thisWriter);
			} else {
				thisWriter.write(c);
			}
		}
	}
}